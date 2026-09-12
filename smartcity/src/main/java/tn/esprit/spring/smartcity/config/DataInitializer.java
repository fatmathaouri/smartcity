package tn.esprit.spring.smartcity.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.spring.smartcity.agent.MunicipalAgent;
import tn.esprit.spring.smartcity.agent.MunicipalAgentRepository;
import tn.esprit.spring.smartcity.agent.DepartmentManager;
import tn.esprit.spring.smartcity.agent.DepartmentManagerRepository;
import tn.esprit.spring.smartcity.auth.Role;
import tn.esprit.spring.smartcity.auth.RoleRepository;
import tn.esprit.spring.smartcity.auth.User;
import tn.esprit.spring.smartcity.auth.UserRepository;
import tn.esprit.spring.smartcity.category.Category;
import tn.esprit.spring.smartcity.category.CategoryRepository;
import tn.esprit.spring.smartcity.category.CategoryService;
import tn.esprit.spring.smartcity.citizen.Citizen;
import tn.esprit.spring.smartcity.citizen.CitizenRepository;
import tn.esprit.spring.smartcity.department.Department;
import tn.esprit.spring.smartcity.department.DepartmentRepository;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CitizenRepository citizenRepository;
    private final MunicipalAgentRepository agentRepository;
    private final DepartmentManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public void run(String... args) {
        seedRoles();
        seedUsers();
        categoryService.seedDefaultCategories();
        seedDepartments();
        ensureAgentAndManagerConsistency();
    }

    private void seedRoles() {
        String[] roles = {"ROLE_CITIZEN", "ROLE_ADMIN", "ROLE_MUNICIPALITY", "ROLE_MUNICIPAL_AGENT", "ROLE_DEPARTMENT_MANAGER"};
        for (String role : roles) {
            if (roleRepository.findByName(role).isEmpty()) {
                roleRepository.save(Role.builder().name(role).build());
            }
        }
    }

    private void seedUsers() {
        // Admin
        createTestUser("admin@smartcity.com", "admin123", "Admin", "SmartCity", "ROLE_ADMIN");

        // Mairie
        createTestUser("mairie@smartcity.com", "mairie123", "Mairie", "Centrale", "ROLE_MUNICIPALITY");

        // Agent
        User agent = createTestUser("agent@smartcity.com", "agent123", "Ahmed", "Ben Ali", "ROLE_MUNICIPAL_AGENT");
        if (agent != null && agentRepository.findByUserId(agent.getId()).isEmpty()) {
            agentRepository.save(MunicipalAgent.builder()
                    .user(agent)
                    .service("Voirie")
                    .zone("Centre Ville")
                    .specialty("Voirie")
                    .disponible(true)
                    .build());
        }

        // Manager
        User manager = createTestUser("manager@smartcity.com", "manager123", "Fatma", "Ben Salah", "ROLE_DEPARTMENT_MANAGER");
        if (manager != null && managerRepository.findByUserId(manager.getId()).isEmpty()) {
            managerRepository.save(DepartmentManager.builder()
                    .user(manager)
                    .serviceGere("Voirie et Routes")
                    .build());
        }

        // Citizen
        createTestUser("citizen@smartcity.com", "citizen123", "Citoyen", "Test", "ROLE_CITIZEN");
    }

    private User createTestUser(String email, String password, String firstName, String lastName, String roleName) {
        if (userRepository.existsByEmail(email)) {
            return userRepository.findByEmail(email).orElse(null);
        }
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));
        User user = userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .roles(Set.of(role))
                .build());
        citizenRepository.save(Citizen.builder().user(user).build());
        return user;
    }

    private void seedDepartments() {
        createDepartmentIfNotExists("Voirie et Routes", "Entretien des routes et trottoirs", "Voirie");
        createDepartmentIfNotExists("Assainissement", "Gestion des eaux usées et drainage", "Assainissement");
        createDepartmentIfNotExists("Éclairage Public", "Entretien de l'éclairage urbain", "Éclairage");
        createDepartmentIfNotExists("Propreté et Environnement", "Collecte des déchets et espaces verts", "Propreté");
        createDepartmentIfNotExists("Espaces Verts", "Parcs, jardins et plantation", "Espaces Verts");
    }

    private void createDepartmentIfNotExists(String name, String description, String categoryName) {
        if (departmentRepository.findByName(name).isEmpty()) {
            categoryRepository.findByName(categoryName).ifPresent(cat ->
                    departmentRepository.save(Department.builder()
                            .name(name)
                            .description(description)
                            .category(cat)
                            .build()));
        }
    }

    private void ensureAgentAndManagerConsistency() {
        log.info("=== Vérification cohérence managers/agents ===");

        java.util.List<User> managers = userRepository
            .findByRoleName("ROLE_DEPARTMENT_MANAGER");

        for (User manager : managers) {
            DepartmentManager dm = managerRepository.findByUserId(manager.getId()).orElse(null);
            if (dm == null) {
                log.warn("Manager sans profil département : {}", manager.getEmail());
            } else {
                if (dm.getDepartment() == null && dm.getServiceGere() != null) {
                    departmentRepository.findByName(dm.getServiceGere()).ifPresent(dept -> {
                        dm.setDepartment(dept);
                        managerRepository.save(dm);
                        log.info("Manager {} lié au département {}", manager.getEmail(), dept.getName());
                    });
                }
                log.info("Manager OK : {}", manager.getEmail());
            }
        }

        java.util.List<User> agents = userRepository
            .findByRoleName("ROLE_MUNICIPAL_AGENT");

        for (User agent : agents) {
            boolean hasProfile = agentRepository
                .existsByUserId(agent.getId());
            if (!hasProfile) {
                log.warn("Agent sans profil département : {}",
                    agent.getEmail());
            } else {
                log.info("Agent OK : {}", agent.getEmail());
            }
        }

        log.info("=== Fin vérification ===");
    }
}
