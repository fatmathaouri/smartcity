package tn.esprit.spring.smartcity.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.spring.smartcity.auth.RoleRepository;
import tn.esprit.spring.smartcity.auth.User;
import tn.esprit.spring.smartcity.auth.UserRepository;
import tn.esprit.spring.smartcity.report.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerDepartmentService {

    private final DepartmentManagerRepository managerRepository;
    private final MunicipalAgentRepository agentRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    @Transactional
    public DepartmentManager createManager(Long userId, String serviceGere) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        DepartmentManager manager = DepartmentManager.builder()
                .user(user)
                .serviceGere(serviceGere)
                .build();
        return managerRepository.save(manager);
    }

    public DepartmentManager getManagerByUserId(Long userId) {
        return managerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Manager not found for user: " + userId));
    }

    public List<MunicipalAgent> getAgentsForService(String service) {
        return agentRepository.findByService(service);
    }

    public Map<String, Object> getDepartmentStats(String service) {
        List<MunicipalAgent> agents = agentRepository.findByService(service);
        List<Long> agentUserIds = agents.stream()
                .map(a -> a.getUser().getId())
                .toList();

        long totalReports = 0;
        long resolved = 0;
        long inProgress = 0;
        long pending = 0;

        for (Long userId : agentUserIds) {
            List<Report> reports = reportRepository.findByAssignedToId(userId);
            totalReports += reports.size();
            for (Report r : reports) {
                switch (r.getTreatmentStatus()) {
                    case RESOLVED, VALIDATED -> resolved++;
                    case IN_PROGRESS -> inProgress++;
                    default -> pending++;
                }
            }
        }

        Map<String, Object> m65 = new java.util.LinkedHashMap<>();
        m65.put("service", service);
        m65.put("agentsCount", agents.size());
        m65.put("totalReports", totalReports);
        m65.put("resolved", resolved);
        m65.put("inProgress", inProgress);
        m65.put("pending", pending);
        m65.put("resolutionRate", totalReports > 0 ? Math.round((resolved * 100.0) / totalReports) : 0);
        return m65;
    }

    public Map<String, Object> suggestAgent(Long reportId, String managerService) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        String category = report.getCategory() != null ? report.getCategory().getName() : "";
        List<MunicipalAgent> available = agentRepository.findByServiceAndDisponibleTrue(managerService);

        MunicipalAgent bestAgent = null;
        double bestScore = Double.MAX_VALUE;

        for (MunicipalAgent agent : available) {
            double score = 0;
            if (agent.getSpecialty() != null && agent.getSpecialty().equalsIgnoreCase(category)) {
                score -= 10;
            }
            score += agent.getInterventionsCount();
            if (agent.getZone() != null && report.getAddress() != null
                    && report.getAddress().toLowerCase().contains(agent.getZone().toLowerCase())) {
                score -= 5;
            }
            if (score < bestScore) {
                bestScore = score;
                bestAgent = agent;
            }
        }

        if (bestAgent == null) {
            return Map.of("suggested", false, "message", "Aucun agent disponible");
        }

        Map<String, Object> m106 = new java.util.LinkedHashMap<>();
        m106.put("suggested", true);
        m106.put("agentId", bestAgent.getId());
        m106.put("userId", bestAgent.getUser().getId());
        m106.put("userName", bestAgent.getUser().getFirstName() + " " + bestAgent.getUser().getLastName());
        m106.put("service", bestAgent.getService());
        m106.put("zone", bestAgent.getZone() != null ? bestAgent.getZone() : "");
        m106.put("specialty", bestAgent.getSpecialty() != null ? bestAgent.getSpecialty() : "");
        m106.put("score", bestScore);
        return m106;
    }

    public List<Map<String, Object>> getAgentPerformances(String service) {
        List<MunicipalAgent> agents = agentRepository.findByService(service);
        return agents.stream()
                .map(a -> {
                    List<Report> reports = reportRepository.findByAssignedToId(a.getUser().getId());
                    long total = reports.size();
                    long resolvedCount = reports.stream()
                            .filter(r -> r.getTreatmentStatus() == TreatmentStatus.RESOLVED || r.getTreatmentStatus() == TreatmentStatus.VALIDATED)
                            .count();
                    Map<String, Object> m127 = new java.util.LinkedHashMap<>();
                    m127.put("userId", a.getUser().getId());
                    m127.put("userName", a.getUser().getFirstName() + " " + a.getUser().getLastName());
                    m127.put("totalAssigned", total);
                    m127.put("resolved", resolvedCount);
                    m127.put("resolutionRate", total > 0 ? Math.round((resolvedCount * 100.0) / total) : 0);
                    m127.put("disponible", a.getDisponible());
                    return m127;
                })
                .collect(Collectors.toList());
    }

    public DepartmentManagerDto toDto(DepartmentManager manager) {
        DepartmentManagerDto dto = new DepartmentManagerDto();
        dto.setId(manager.getId());
        dto.setUserId(manager.getUser().getId());
        dto.setUserName(manager.getUser().getFirstName() + " " + manager.getUser().getLastName());
        dto.setEmail(manager.getUser().getEmail());
        dto.setServiceGere(manager.getServiceGere());
        if (manager.getDepartment() != null) {
            dto.setDepartmentId(manager.getDepartment().getId());
        }
        return dto;
    }

    public List<Report> getUnassignedReports(Long managerUserId) {
        DepartmentManager manager = managerRepository.findByUserId(managerUserId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));
        Long categoryId = manager.getDepartment().getCategory().getId();
        return reportRepository.findByCategoryIdAndAssignedToIsNullAndTreatmentStatus(categoryId, TreatmentStatus.NEW);
    }

    @Transactional
    public void deleteAgent(Long agentId, Long managerUserId) {
        MunicipalAgent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent introuvable"));

        DepartmentManager manager = managerRepository.findByUserId(managerUserId)
                .orElseThrow(() -> new RuntimeException("Manager non trouvé"));

        if (manager.getDepartment() == null || agent.getDepartment() == null
                || !manager.getDepartment().getId().equals(agent.getDepartment().getId())) {
            throw new RuntimeException("Cet agent n'appartient pas à votre département");
        }

        User agentUser = agent.getUser();
        agentRepository.delete(agent);
        userRepository.delete(agentUser);
    }

    @Transactional
    public MunicipalAgent updateAgent(Long agentId, String zone, String specialty, Long managerUserId) {
        MunicipalAgent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent introuvable"));

        DepartmentManager manager = managerRepository.findByUserId(managerUserId)
                .orElseThrow(() -> new RuntimeException("Manager non trouvé"));

        if (manager.getDepartment() == null || agent.getDepartment() == null
                || !manager.getDepartment().getId().equals(agent.getDepartment().getId())) {
            throw new RuntimeException("Cet agent n'appartient pas à votre département");
        }

        if (zone != null) agent.setZone(zone);
        if (specialty != null) agent.setSpecialty(specialty);
        return agentRepository.save(agent);
    }
}
