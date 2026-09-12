package tn.esprit.spring.smartcity.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.spring.smartcity.auth.User;
import tn.esprit.spring.smartcity.auth.UserRepository;
import tn.esprit.spring.smartcity.report.Report;
import tn.esprit.spring.smartcity.report.ReportRepository;
import tn.esprit.spring.smartcity.report.TreatmentStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final MunicipalAgentRepository agentRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    @Transactional
    public MunicipalAgent createAgent(Long userId, String service, String zone, String specialty) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        MunicipalAgent agent = MunicipalAgent.builder()
                .user(user)
                .service(service)
                .zone(zone)
                .specialty(specialty)
                .build();
        return agentRepository.save(agent);
    }

    public MunicipalAgent getAgentByUserId(Long userId) {
        return agentRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Agent not found for user: " + userId));
    }

    public List<MunicipalAgent> getAllAgents() {
        return agentRepository.findAll();
    }

    public List<MunicipalAgent> getAgentsByService(String service) {
        return agentRepository.findByService(service);
    }

    public List<MunicipalAgent> getAvailableAgents() {
        return agentRepository.findByDisponibleTrue();
    }

    @Transactional
    public MunicipalAgent toggleDisponibilite(Long userId) {
        MunicipalAgent agent = getAgentByUserId(userId);
        agent.setDisponible(!agent.getDisponible());
        return agentRepository.save(agent);
    }

    @Transactional
    public MunicipalAgent incrementInterventions(Long userId) {
        MunicipalAgent agent = getAgentByUserId(userId);
        agent.setInterventionsCount(agent.getInterventionsCount() + 1);
        return agentRepository.save(agent);
    }

    public Map<String, Object> getAgentPerformance(Long userId) {
        MunicipalAgent agent = getAgentByUserId(userId);
        List<Report> assigned = reportRepository.findByAssignedToId(userId);

        long total = assigned.size();
        long resolved = assigned.stream()
                .filter(r -> r.getTreatmentStatus() == TreatmentStatus.RESOLVED || r.getTreatmentStatus() == TreatmentStatus.VALIDATED)
                .count();
        long inProgress = assigned.stream()
                .filter(r -> r.getTreatmentStatus() == TreatmentStatus.IN_PROGRESS)
                .count();
        long pending = assigned.stream()
                .filter(r -> r.getTreatmentStatus() == TreatmentStatus.NEW || r.getTreatmentStatus() == TreatmentStatus.ACCEPTED)
                .count();

        Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("agentId", agent.getId());
        m.put("userName", agent.getUser().getFirstName() + " " + agent.getUser().getLastName());
        m.put("service", agent.getService());
        m.put("zone", agent.getZone() != null ? agent.getZone() : "");
        m.put("disponible", agent.getDisponible());
        m.put("totalAssigned", total);
        m.put("resolved", resolved);
        m.put("inProgress", inProgress);
        m.put("pending", pending);
        m.put("resolutionRate", total > 0 ? Math.round((resolved * 100.0) / total) : 0);
        return m;
    }

    public MunicipalAgentDto toDto(MunicipalAgent agent) {
        MunicipalAgentDto dto = new MunicipalAgentDto();
        dto.setId(agent.getId());
        dto.setUserId(agent.getUser().getId());
        dto.setUserName(agent.getUser().getFirstName() + " " + agent.getUser().getLastName());
        dto.setEmail(agent.getUser().getEmail());
        dto.setService(agent.getService());
        dto.setZone(agent.getZone());
        dto.setSpecialty(agent.getSpecialty());
        dto.setDisponible(agent.getDisponible());
        dto.setInterventionsCount(agent.getInterventionsCount());
        return dto;
    }
}
