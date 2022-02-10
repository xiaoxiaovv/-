package com.istar.mediabroken.service.account

import com.istar.mediabroken.entity.account.SimulateLoginSession
import com.istar.mediabroken.repo.account.SimulateLoginSessionRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SimulateLoginSessionService {
    @Autowired
    SimulateLoginSessionRepo simulateLoginSessionRepo

    String addSimulateLoginSession(long userId, long adminUserId, String adminUsername) {
        SimulateLoginSession simulateLoginSession = new SimulateLoginSession(
                UUID.randomUUID().toString(),
                userId,
                adminUserId,
                adminUsername,
                true,
                new Date(),
                new Date()
        );
        String sid = simulateLoginSessionRepo.addSimulateLoginSession(simulateLoginSession)
        return sid
    }

    Map getUserBySimulateSessionId(String sid) {
        return simulateLoginSessionRepo.getUserBySimulateSessionId(sid)
    }

    void disableSimulateSession(long userId) {
        simulateLoginSessionRepo.disableSimulateSession(userId)
    }

    void disableSimulateSession(String sid) {
        simulateLoginSessionRepo.disableSimulateSession(sid)
    }

    void deleteSimulateSession(String sid) {
        simulateLoginSessionRepo.deleteSimulateSession(sid)
    }
}
