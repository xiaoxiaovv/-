package com.istar.mediabroken.service.account

import com.istar.mediabroken.repo.account.SimulateUserSecretKeyRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SimulateUserSecretKeyService {
    @Autowired
    SimulateUserSecretKeyRepo simulateUserSecretKeyRepo

    boolean securityVisitByUserIdAndSecretKey(long userId, String secretKey) {
        def info = simulateUserSecretKeyRepo.getInfoByUserIdAndSecretKey(userId, secretKey)
        return info ? true : false
    }
}
