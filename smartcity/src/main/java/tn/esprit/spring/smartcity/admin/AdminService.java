package tn.esprit.spring.smartcity.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.spring.smartcity.agent.DepartmentManager;
import tn.esprit.spring.smartcity.agent.DepartmentManagerRepository;
import tn.esprit.spring.smartcity.agent.MunicipalAgent;
import tn.esprit.spring.smartcity.agent.MunicipalAgentRepository;
import tn.esprit.spring.smartcity.auth.*;
import tn.esprit.spring.smartcity.category.Category;
import tn.esprit.spring.smartcity.category.CategoryRepository;
import tn.esprit.spring.smartcity.citizen.CitizenRepository;
import tn.esprit.spring.smartcity.config.AdminLimitsConfig;
import tn.esprit.spring.smartcity.department.Department;
import tn.esprit.spring.smartcity.department.DepartmentRepository;
import tn.esprit.spring.smartcity.report.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final CitizenRepository citizenRepository;
    private final CategoryRepository categoryRepository;
    private final ReportRepository reportRepository;
    private final AuthService authService;
    private final AdminLimitsConfig limitsConfig;
    private final DepartmentManagerRepository managerRepository;
    private final MunicipalAgentRepository agentRepository;
    private final DepartmentRepository departmentRepository;

    // --- User management ---

    public List<Map<String, Object>> getAllUsers() {
        List<Map<String, Object>> users = new ArrayList<>();
        userRepository.findAll().forEach(user -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", user.getId());
            map.put("email", user.getEmail());
            map.put("firstName", user.getFirstName());
            map.put("lastName", user.getLastName());
            map.put("phone", user.getPhone());
            map.put("enabled", user.isEnabled());
            map.put("mustChangePassword", user.getMustChangePassword());
            map.put("createdAt", user.getCreatedAt());
            map.put("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
            users.add(map);
        });
        return users;
    }

    public Map<String, Object> getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("email", user.getEmail());
        map.put("firstName", user.getFirstName());
        map.put("lastName", user.getLastName());
        map.put("phone", user.getPhone());
        map.put("enabled", user.isEnabled());
        map.put("mustChangePassword", user.getMustChangePassword());
        map.put("createdAt", user.getCreatedAt());
        map.put("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
        return map;
    }

    @Transactional
    public UserCreationResult createUser(CreateUserRequest request) {
        long currentCount = roleAssignmentRepository.countByRoleIdAndRevokedAtIsNull(
                roleRepository.findByName(request.getRoleName()).map(Role::getId).orElse(0L));
        Integer max = getRoleLimit(request.getRoleName());
        if (max != null && currentCount >= max) {
            throw new RuntimeException("Role limit reached: " + request.getRoleName() + " (max " + max + ")");
        }
        if (("ROLE_MUNICIPAL_AGENT".equals(request.getRoleName()) || "ROLE_DEPARTMENT_MANAGER".equals(request.getRoleName()))
                && request.getDepartmentId() == null) {
            throw new RuntimeException("Department required for role: " + request.getRoleName());
        }
        if ("ROLE_DEPARTMENT_MANAGER".equals(request.getRoleName()) && request.getDepartmentId() != null) {
            if (managerRepository.existsByDepartmentId(request.getDepartmentId())) {
                throw new RuntimeException("Ce département a déjà un manager");
            }
        }

        return authService.createUserInternal(
                request.getEmail(), request.getFirstName(), request.getLastName(),
                request.getPhone(), request.getRoleName(), request.getDepartmentId());
    }

    @Transactional
    public Map<String, Object> updateUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));
        user.getRoles().add(role);
        userRepository.save(user);

        RoleAssignment assignment = RoleAssignment.builder()
                .user(user)
                .role(role)
                .assignedBy(user)
                .build();
        roleAssignmentRepository.save(assignment);

        log.info("Role {} added to user {}", roleName, user.getEmail());
        return getUserById(userId);
    }

    @Transactional
    public Map<String, Object> removeUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.getRoles().removeIf(r -> r.getName().equals(roleName));
        userRepository.save(user);

        Role role = roleRepository.findByName(roleName).orElse(null);
        if (role != null) {
            RoleAssignment revocation = RoleAssignment.builder()
                    .user(user)
                    .role(role)
                    .assignedBy(user)
                    .revokedAt(java.time.LocalDateTime.now())
                    .build();
            roleAssignmentRepository.save(revocation);
        }

        if ("ROLE_MUNICIPAL_AGENT".equals(roleName)) {
            agentRepository.findByUserId(userId).ifPresent(e -> agentRepository.delete(e));
        } else if ("ROLE_DEPARTMENT_MANAGER".equals(roleName)) {
            managerRepository.findByUserId(userId).ifPresent(e -> managerRepository.delete(e));
        }

        log.info("Role {} removed from user {}", roleName, user.getEmail());
        return getUserById(userId);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        citizenRepository.findByUserId(userId).ifPresent(e -> citizenRepository.delete(e));
        agentRepository.findByUserId(userId).ifPresent(e -> agentRepository.delete(e));
        managerRepository.findByUserId(userId).ifPresent(e -> managerRepository.delete(e));
        userRepository.delete(user);
    }

    @Transactional
    public Map<String, Object> toggleUserEnabled(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return getUserById(userId);
    }

    @Transactional
    public UserCreationResult resetTempPassword(Long userId) {
        return authService.resetTempPassword(userId);
    }

    public List<Map<String, Object>> getRoleAssignments() {
        List<Map<String, Object>> assignments = new ArrayList<>();
        roleAssignmentRepository.findAll().forEach(ra -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", ra.getId());
            map.put("userId", ra.getUser().getId());
            map.put("userEmail", ra.getUser().getEmail());
            map.put("userName", ra.getUser().getFirstName() + " " + ra.getUser().getLastName());
            map.put("roleName", ra.getRole().getName());
            map.put("assignedBy", ra.getAssignedBy().getFirstName() + " " + ra.getAssignedBy().getLastName());
            map.put("assignedAt", ra.getAssignedAt());
            map.put("revokedAt", ra.getRevokedAt());
            map.put("reason", ra.getReason());
            assignments.add(map);
        });
        return assignments;
    }

    // --- Category management ---

    public Category createCategory(String name, String description, String priority) {
        Category cat = Category.builder()
                .name(name)
                .description(description)
                .defaultPriority(priority != null ? Priority.valueOf(priority) : Priority.MEDIUM)
                .build();
        return categoryRepository.save(cat);
    }

    public Category updateCategory(Long id, String name, String description, String priority) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        if (name != null) cat.setName(name);
        if (description != null) cat.setDescription(description);
        if (priority != null) cat.setDefaultPriority(Priority.valueOf(priority));
        return categoryRepository.save(cat);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    // --- Stats/analytics ---

    public List<Map<String, Object>> getHeatmapData() {
        List<Report> reports = reportRepository.findAll();
        Map<String, Integer> zoneCounts = new LinkedHashMap<>();
        for (Report r : reports) {
            if (r.getLatitude() != null && r.getLongitude() != null) {
                String zone = String.format("%.2f,%.2f",
                        Math.round(r.getLatitude() * 100.0) / 100.0,
                        Math.round(r.getLongitude() * 100.0) / 100.0);
                zoneCounts.merge(zone, 1, Integer::sum);
            }
        }
        return zoneCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(20)
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("zone", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> getGlobalStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalReports", reportRepository.count());
        stats.put("pending", reportRepository.countByStatus(ReportStatus.PENDING));
        stats.put("inProgress", reportRepository.countByStatus(ReportStatus.IN_PROGRESS));
        stats.put("resolved", reportRepository.countByStatus(ReportStatus.RESOLVED));
        stats.put("totalUsers", userRepository.count());

        long total = reportRepository.count();
        long resolved = reportRepository.countByStatus(ReportStatus.RESOLVED);
        stats.put("resolutionRate", total > 0 ? Math.round((resolved * 100.0) / total) : 0);

        List<Object[]> byCategory = reportRepository.countByCategory();
        Map<String, Long> categoryMap = new LinkedHashMap<>();
        for (Object[] row : byCategory) {
            Long catId = (Long) row[0];
            Long count = (Long) row[1];
            Category cat = categoryRepository.findById(catId).orElse(null);
            String name = cat != null ? cat.getName() : "Inconnu";
            categoryMap.put(name, count);
        }
        stats.put("reportsByCategory", categoryMap);
        return stats;
    }

    public List<Map<String, Object>> getReportsByMonth() {
        List<Object[]> data = reportRepository.countByMonth();
        List<Map<String, Object>> result = new ArrayList<>();
        String[] months = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        for (Object[] row : data) {
            Map<String, Object> m = new LinkedHashMap<>();
            int month = ((Number) row[0]).intValue();
            m.put("month", month <= 12 ? months[month - 1] : "Mois " + month);
            m.put("count", row[1]);
            result.add(m);
        }
        return result;
    }

    public List<Map<String, Object>> getDepartmentsManagerStatus() {
        List<Map<String, Object>> result = new ArrayList<>();
        departmentRepository.findAll().forEach(dept -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", dept.getId());
            m.put("name", dept.getName());
            m.put("description", dept.getDescription());
            m.put("hasManager", managerRepository.existsByDepartmentId(dept.getId()));
            result.add(m);
        });
        return result;
    }

    @Transactional
    public Map<String, Object> reassignManagerDepartment(Long userId, Long newDepartmentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        DepartmentManager manager = managerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cet utilisateur n'est pas un manager de département"));

        Department newDept = departmentRepository.findById(newDepartmentId)
                .orElseThrow(() -> new RuntimeException("Département introuvable"));

        Department oldDept = manager.getDepartment();

        DepartmentManager otherManager = managerRepository.findByDepartmentId(newDepartmentId).orElse(null);

        if (otherManager != null && otherManager.getId().equals(manager.getId())) {
            throw new RuntimeException("Ce manager est déjà assigné à ce département");
        }

        if (otherManager != null) {
            otherManager.setDepartment(oldDept);
            otherManager.setServiceGere(oldDept != null ? oldDept.getName() : "");
            managerRepository.save(otherManager);
            log.info("Swap: manager {} déplacé de {} vers {}", otherManager.getUser().getEmail(),
                    newDept.getName(), oldDept != null ? oldDept.getName() : "vide");
        }

        manager.setDepartment(newDept);
        manager.setServiceGere(newDept.getName());
        managerRepository.save(manager);

        log.info("Manager {} réassigné au département {}", user.getEmail(), newDept.getName());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", user.getId());
        result.put("userName", user.getFirstName() + " " + user.getLastName());
        result.put("departmentId", newDept.getId());
        result.put("departmentName", newDept.getName());
        result.put("swapped", otherManager != null);
        if (otherManager != null) {
            result.put("swappedWith", otherManager.getUser().getFirstName() + " " + otherManager.getUser().getLastName());
            result.put("swappedTo", oldDept != null ? oldDept.getName() : "Aucun");
        }
        return result;
    }

    private Integer getRoleLimit(String roleName) {
        return switch (roleName) {
            case "ROLE_ADMIN" -> limitsConfig.getMaxAdmins();
            case "ROLE_MUNICIPALITY" -> limitsConfig.getMaxMunicipality();
            case "ROLE_DEPARTMENT_MANAGER" -> limitsConfig.getMaxManagers();
            case "ROLE_MUNICIPAL_AGENT" -> limitsConfig.getMaxAgentsTotal();
            default -> null;
        };
    }
}
