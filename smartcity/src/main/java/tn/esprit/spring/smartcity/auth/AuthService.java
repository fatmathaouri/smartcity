package tn.esprit.spring.smartcity.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.spring.smartcity.agent.DepartmentManager;
import tn.esprit.spring.smartcity.agent.DepartmentManagerRepository;
import tn.esprit.spring.smartcity.agent.MunicipalAgent;
import tn.esprit.spring.smartcity.agent.MunicipalAgentRepository;
import tn.esprit.spring.smartcity.citizen.Citizen;
import tn.esprit.spring.smartcity.citizen.CitizenRepository;
import tn.esprit.spring.smartcity.config.AdminLimitsConfig;
import tn.esprit.spring.smartcity.config.JwtUtil;
import tn.esprit.spring.smartcity.department.Department;
import tn.esprit.spring.smartcity.department.DepartmentRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final CitizenRepository citizenRepository;
    private final MunicipalAgentRepository agentRepository;
    private final DepartmentManagerRepository managerRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AdminLimitsConfig limitsConfig;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        Role citizenRole = roleRepository.findByName("ROLE_CITIZEN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_CITIZEN").build()));

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .roles(Set.of(citizenRole))
                .build();
        user = userRepository.save(user);

        Citizen citizen = Citizen.builder()
                .user(user)
                .address(request.getAddress())
                .city(request.getCity())
                .build();
        citizenRepository.save(citizen);

        List<String> roles = List.of("ROLE_CITIZEN");
        String token = jwtUtil.generateToken(user.getEmail(), roles);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .mustChangePassword(false)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmailWithRoles(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.TRUE.equals(user.getMustChangePassword())
                && user.getTempPasswordExpiresAt() != null
                && LocalDateTime.now().isAfter(user.getTempPasswordExpiresAt())) {
            throw new RuntimeException("Mot de passe temporaire expiré. Contactez votre administrateur.");
        }

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        log.info("Login user={}, roles={}", user.getEmail(), roles);
        String token = jwtUtil.generateToken(user.getEmail(), roles);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .mustChangePassword(Boolean.TRUE.equals(user.getMustChangePassword()))
                .build();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        user.setTempPasswordExpiresAt(null);
        userRepository.save(user);
        log.info("Password changed for user {}", user.getEmail());
    }

    @Transactional
    public UserCreationResult createUserInternal(String email, String firstName,
            String lastName, String phone, String roleName, Long departmentId) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        String tempPassword = generateTemporaryPassword();
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(tempPassword))
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .roles(Set.of(role))
                .mustChangePassword(true)
                .tempPasswordExpiresAt(LocalDateTime.now().plusHours(48))
                .build();
        user = userRepository.save(user);

        Department dept = null;
        String deptName = null;
        if (departmentId != null) {
            dept = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new RuntimeException("Département introuvable avec l'identifiant: " + departmentId));
            deptName = dept.getName();
        }

        if ("ROLE_DEPARTMENT_MANAGER".equals(roleName)) {
            if (dept == null) {
                throw new RuntimeException("Département requis pour le rôle ROLE_DEPARTMENT_MANAGER");
            }
            if (managerRepository.existsByDepartmentId(departmentId)) {
                throw new RuntimeException("Ce département a déjà un manager");
            }
        }

        if ("ROLE_MUNICIPAL_AGENT".equals(roleName) && dept != null) {
            String service = dept.getName();
            agentRepository.save(MunicipalAgent.builder()
                    .user(user)
                    .service(service)
                    .department(dept)
                    .build());
        } else if ("ROLE_DEPARTMENT_MANAGER".equals(roleName) && dept != null) {
            managerRepository.save(DepartmentManager.builder()
                    .user(user)
                    .serviceGere(dept.getName())
                    .department(dept)
                    .build());
        }

        log.info("User created: {} with role {} by admin", email, roleName);

        return UserCreationResult.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getFirstName() + " " + user.getLastName())
                .temporaryPassword(tempPassword)
                .role(roleName)
                .departmentName(deptName)
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional
    public UserCreationResult createUserByManager(String email, String firstName,
            String lastName, String phone, Long departmentId, Long managerUserId) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        DepartmentManager manager = managerRepository.findByUserId(managerUserId)
                .orElseThrow(() -> new RuntimeException(
                    "Votre profil manager de département est introuvable. Contactez votre administrateur."));

        if (manager.getDepartment() == null) {
            throw new RuntimeException("Aucun département associé à votre profil. Contactez votre administrateur.");
        }

        if (!manager.getDepartment().getId().equals(departmentId)) {
            throw new RuntimeException("Vous ne pouvez créer des agents que dans votre département (" + manager.getDepartment().getName() + ")");
        }

        long agentCount = agentRepository.countByDepartmentId(departmentId);
        if (agentCount >= limitsConfig.getAgentsPerDepartment()) {
            throw new RuntimeException("Ce département a atteint la limite maximale de " + limitsConfig.getAgentsPerDepartment() + " agents");
        }

        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Département introuvable avec l'identifiant: " + departmentId));

        String tempPassword = generateTemporaryPassword();
        Role agentRole = roleRepository.findByName("ROLE_MUNICIPAL_AGENT")
                .orElseThrow(() -> new RuntimeException("ROLE_MUNICIPAL_AGENT not found"));

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(tempPassword))
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .roles(Set.of(agentRole))
                .mustChangePassword(true)
                .tempPasswordExpiresAt(LocalDateTime.now().plusHours(48))
                .build();
        user = userRepository.save(user);

        agentRepository.save(MunicipalAgent.builder()
                .user(user)
                .service(dept.getName())
                .department(dept)
                .build());

        RoleAssignment assignment = RoleAssignment.builder()
                .user(user)
                .role(agentRole)
                .assignedBy(userRepository.findById(managerUserId).orElse(null))
                .build();
        roleAssignmentRepository.save(assignment);

        log.info("Agent {} created by manager {}", email, managerUserId);

        return UserCreationResult.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getFirstName() + " " + user.getLastName())
                .temporaryPassword(tempPassword)
                .role("ROLE_MUNICIPAL_AGENT")
                .departmentName(dept.getName())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional
    public UserCreationResult resetTempPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String tempPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setMustChangePassword(true);
        user.setTempPasswordExpiresAt(LocalDateTime.now().plusHours(48));
        userRepository.save(user);

        log.info("Temp password reset for user {}", user.getEmail());

        return UserCreationResult.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getFirstName() + " " + user.getLastName())
                .temporaryPassword(tempPassword)
                .role(user.getRoles().stream().findFirst().map(Role::getName).orElse("UNKNOWN"))
                .createdAt(user.getCreatedAt())
                .build();
    }

    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789@#$!";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder("SmartCity@");
        for (int i = 0; i < 5; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        for (int i = 0; i < 2; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }
}
