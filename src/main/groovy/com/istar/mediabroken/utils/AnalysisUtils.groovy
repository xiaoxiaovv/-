package com.istar.mediabroken.utils

/**
 * Author: Luda
 * Time: 2017/8/9
 */
class AnalysisUtils {

    static BigDecimal getPSIBigDecimal(int postWEB, int postBBS, int postWechat, int postWeibo, int postApps, int postMount,
                                       int reprintWEB, int reprintBBS, int reprintWeibo, int reprintWechat, int reprintMount,
                                       int meidaMount, int meidaImportant) {
        if (postWEB <= 0 || postBBS <= 0 || postWechat <= 0 || postWeibo <= 0 || postApps <= 0 || postMount <= 0 ||
                reprintWEB <= 0 || reprintBBS <= 0 || reprintWeibo <= 0 || reprintWechat <= 0 || reprintMount <= 0 ||
                meidaMount <= 0 || meidaImportant <= 0) {
            return null
        }
        //PSI= 40%*(10%*ln(PostWEB)+10%*ln(PostBBS)+10%*ln(PostWechat)+10%*ln(PostWeibo)+10%*ln(PostApps)+50%*ln(PostMount))+50%*(20%*ln(ReprintWEB)+10%*ln(ReprintBBS)+20%*ln(TReprintWeibo)+10%*ln(ReprintWechat)+40%*ln(ReprintMount))+10%*(60*ln(MeidaMount)+40*ln(MeidaImportant))
        def powerPostWEB = (Math.log(postWEB) as BigDecimal).multiply(0.1)
        def powerPostBBS = (Math.log(postBBS) as BigDecimal).multiply(0.1)
        def powerPostWechat = (Math.log(postWechat) as BigDecimal).multiply(0.1)
        def powerPostWeibo = (Math.log(postWeibo) as BigDecimal).multiply(0.1)
        def powerPostApps = (Math.log(postApps) as BigDecimal).multiply(0.1)
        def powerPostMount = (Math.log(postMount) as BigDecimal).multiply(0.5)
        def powerPost = powerPostWEB.add(powerPostBBS).add(powerPostWechat).add(powerPostWeibo).add(powerPostApps).add(powerPostMount).multiply(0.4)

        def powerReprintWEB = (Math.log(reprintWEB) as BigDecimal).multiply(0.2)
        def powerReprintBBS = (Math.log(reprintBBS) as BigDecimal).multiply(0.1)
        def powerReprintWeibo = (Math.log(reprintWeibo) as BigDecimal).multiply(0.2)
        def powerReprintWechat = (Math.log(reprintWechat) as BigDecimal).multiply(0.1)
        def powerReprintMount = (Math.log(reprintMount) as BigDecimal).multiply(0.4)
        def powerReprint = powerReprintWEB.add(powerReprintBBS).add(powerReprintWeibo).add(powerReprintWechat).add(powerReprintMount).multiply(0.5)

        def powerMeidaMount = (Math.log(meidaMount) as BigDecimal).multiply(0.6)
        def powerMeidaImportant = (Math.log(meidaImportant) as BigDecimal).multiply(0.4)
        def powerMeida = powerMeidaMount.add(powerMeidaImportant).multiply(0.1)

        return powerPost.add(powerReprint).add(powerMeida)
    }

    static double getPSI(Map<String, Long> parameter) {
        //PSI= 40%*(10%*ln(PostWEB)+10%*ln(PostBBS)+10%*ln(PostWechat)+10%*ln(PostWeibo)+10%*ln(PostApps)+50%*ln(PostMount))+50%*(20%*ln(ReprintWEB)+10%*ln(ReprintBBS)+20%*ln(TReprintWeibo)+10%*ln(ReprintWechat)+40%*ln(ReprintMount))+10%*(60*ln(MeidaMount)+40*ln(MeidaImportant))
        def powerPostWEB = parameter.postWEB && parameter.postWEB > 0 ? (Math.log(parameter.postWEB+1)) * 0.1 : 0
        def powerPostBBS = parameter.postBBS && parameter.postBBS > 0 ? (Math.log(parameter.postBBS+1)) * 0.1 : 0
        def powerPostWechat = parameter.postWechat && parameter.postWechat > 0 ? (Math.log(parameter.postWechat+1)) * 0.1 : 0
        def powerPostWeibo = parameter.postWeibo && parameter.postWeibo > 0 ? (Math.log(parameter.postWeibo+1)) * 0.1 : 0
        def powerPostApps = parameter.postApps && parameter.postApps > 0 ? (Math.log(parameter.postApps+1)) * 0.1 : 0
        def powerPostMount = parameter.postMount && parameter.postMount > 0 ? (Math.log(parameter.postMount+1)) * 0.5 : 0
        def powerPost = (powerPostWEB + powerPostBBS + powerPostWechat + powerPostWeibo + powerPostApps + powerPostMount) * 0.4

        def powerReprintWEB = parameter.reprintWEB && parameter.reprintWEB > 0 ? (Math.log(parameter.reprintWEB+1)) * 0.2 : 0
        def powerReprintBBS = parameter.reprintBBS && parameter.reprintBBS > 0 ? (Math.log(parameter.reprintBBS+1)) * 0.1 : 0
        def powerReprintWeibo = parameter.reprintWeibo && parameter.reprintWeibo > 0 ? (Math.log(parameter.reprintWeibo+1)) * 0.2 : 0
        def powerReprintWechat = parameter.reprintWechat && parameter.reprintWechat > 0 ? (Math.log(parameter.reprintWechat+1)) * 0.1 : 0
        def powerReprintMount = parameter.reprintMount && parameter.reprintMount > 0 ? (Math.log(parameter.reprintMount+1)) * 0.4 : 0
        def powerReprint = (powerReprintWEB + powerReprintBBS + powerReprintWeibo + powerReprintWechat + powerReprintMount) * 0.5

        def powerMeidaMount = parameter.mediaMount && parameter.mediaMount > 0 ? (Math.log(parameter.mediaMount+1)) * 0.6 : 0
        def powerMeidaImportant = parameter.mediaImportant && parameter.mediaImportant > 0 ? (Math.log(parameter.mediaImportant+1)) * 0.4 : 0
        def powerMeida = (powerMeidaMount + powerMeidaImportant) * 0.1

        return powerPost + powerReprint + powerMeida
    }

    static double getMII(Map<String, Double> parameter) {
        //MII=60%*(40%*ln(Rpd)+40%*ln(Rav)+10%*ln(Rmax)+10%*ln(Rmount))+20%*(40%*ln(Ppd)+40%*ln(Pav)+10%*ln(Pmax)+10%*ln(Pmount))+10%*(40%*ln(Zpd)+40%*ln(Zav)+10%*ln(Zmax)+10%*ln(Zmount))+10%*(60*ln(MM)+40*ln(MI))
        def powerRpd = parameter.rpd && parameter.rpd > 0 ? (Math.log(parameter.rpd+1)) * 0.4 : 0
        def powerRav = parameter.rav && parameter.rav > 0 ? (Math.log(parameter.rav+1)) * 0.4 : 0
        def powerRmax = parameter.rmax && parameter.rmax > 0 ? (Math.log(parameter.rmax+1)) * 0.1 : 0
        def powerRmount = parameter.rmount && parameter.rmount > 0 ? (Math.log(parameter.rmount+1)) * 0.1 : 0
        def powerR = (powerRpd + powerRav + powerRmax + powerRmount) * 0.6

        def powerPpd = parameter.ppd && parameter.ppd > 0 ? (Math.log(parameter.ppd+1)) * 0.4 : 0
        def powerPav = parameter.pav && parameter.pav > 0 ? (Math.log(parameter.pav+1)) * 0.4 : 0
        def powerPmax = parameter.pmax && parameter.pmax > 0 ? (Math.log(parameter.pmax+1)) * 0.1 : 0
        def powerPmount = parameter.pmount && parameter.pmount > 0 ? (Math.log(parameter.pmount+1)) * 0.1 : 0
        def powerP = (powerPpd + powerPav + powerPmax + powerPmount) * 0.2

        def powerZpd = parameter.zpd && parameter.zpd > 0 ? (Math.log(parameter.zpd+1)) * 0.4 : 0
        def powerZav = parameter.zav && parameter.zav > 0 ? (Math.log(parameter.zav+1)) * 0.4 : 0
        def powerZmax = parameter.zmax && parameter.zmax > 0 ? (Math.log(parameter.zmax+1)) * 0.1 : 0
        def powerZmount = parameter.zmount && parameter.zmount > 0 ? (Math.log(parameter.zmount+1)) * 0.1 : 0
        def powerZ = (powerZpd + powerZav + powerZmax + powerZmount) * 0.1

        def powerMM = parameter.mM && parameter.mM > 0 ? (Math.log(parameter.mM+1)) * 0.6 : 0
        def powerMI = parameter.mI && parameter.mI > 0 ? (Math.log(parameter.mI+1)) * 0.4 : 0
        def powerM = (powerMM + powerMI) * 0.1
        return powerR + powerP + powerZ + powerM
    }

    static double getBSI(Map<String, Double> parameter) {
        //BSI= 10%*(40%*ln(Rpd)+40%*ln(Rav)+10%*ln(Rmax)+10%*ln(Rmount))
        //    +40%*(40%*ln(Pmount)+60%*ln(PosMount)-40%*ln(NagMount))
        //    +10%*(40%*ln(Ppd)+40%*ln(Pav)+10%*ln(Pmax)+10%*ln(Pmount))
        //    +40%*(20%*ln(Keymatch)+60%*ln(keyInnovation)+20%*ln(KeyPropagation))

        def powerRpd = parameter.rpd && parameter.rpd > 0 ? (Math.log(parameter.rpd+1)) * 0.4 : 0
        def powerRav = parameter.rav && parameter.rav > 0 ? (Math.log(parameter.rav+1)) * 0.4 : 0
        def powerRmax = parameter.rmax && parameter.rmax > 0 ? (Math.log(parameter.rmax+1)) * 0.1 : 0
        def powerRmount = parameter.rmount && parameter.rmount > 0 ? (Math.log(parameter.rmount+1)) * 0.1 : 0
        def powerR = (powerRpd + powerRav + powerRmax + powerRmount) * 0.05

        def powerZpd = parameter.zpd && parameter.zpd > 0 ? (Math.log(parameter.zpd+1)) * 0.4 : 0
        def powerZav = parameter.zav && parameter.zav > 0 ? (Math.log(parameter.zav+1)) * 0.4 : 0
        def powerZmax = parameter.zmax && parameter.zmax > 0 ? (Math.log(parameter.zmax+1)) * 0.1 : 0
        def powerZmount = parameter.zmount && parameter.zmount > 0 ? (Math.log(parameter.zmount+1)) * 0.1 : 0
        def powerZ = (powerZpd + powerZav + powerZmax + powerZmount) * 0.05

        def powerPpd = parameter.ppd && parameter.ppd > 0 ? (Math.log(parameter.ppd+1)) * 0.4 : 0
        def powerPav = parameter.pav && parameter.pav > 0 ? (Math.log(parameter.pav+1)) * 0.4 : 0
        def powerPmax = parameter.pmax && parameter.pmax > 0 ? (Math.log(parameter.pmax+1)) * 0.1 : 0
        def powerPmount = parameter.pmount && parameter.pmount > 0 ? (Math.log(parameter.pmount+1)) * 0.1 : 0
        def powerP = (powerPpd + powerPav + powerPmax + powerPmount) * 0.1

        //sentiments
        def powerSPmount = parameter.sPmount && parameter.sPmount > 0 ? (Math.log(parameter.sPmount+1)) * 0.4 : 0
        def powerSPosMount = parameter.sPosMount && parameter.sPosMount > 0 ? (Math.log(parameter.sPosMount+1)) * 0.6 : 0
        def powerSNagMount = parameter.sNagMount && parameter.sNagMount > 0 ? (Math.log(parameter.sNagMount+1)) * 0.4 : 0
        def powerS = (powerSPmount + powerSPosMount - powerSNagMount) * 0.4

        def powerKeyMatch = parameter.keyMatch && parameter.keyMatch > 0 ? (Math.log(parameter.keyMatch+1)) * 0.2 : 0
        def powerKeyInnovation = parameter.keyInnovation && parameter.keyInnovation > 0 ? (Math.log(parameter.keyInnovation+1)) * 0.6 : 0
        def powerKeyPropagation = parameter.keyPropagation && parameter.keyPropagation > 0 ? (Math.log(parameter.keyPropagation+1)) * 0.2 : 0
        def powerK = (powerKeyMatch + powerKeyInnovation + powerKeyPropagation) * 0.4

        return powerR + powerP + powerZ + powerS + powerK
    }

    static double getTSI(Map<String, Double> parameter) {
        //TSI= 30%*(10%*ln(Cmount)+20%*ln(Tmount)+5%*ln(Smount)+5%*ln(Wmount)+60%*ln(Imount))+20%*(40%*ln(Pmount)+60%*ln(PosMount)-40%*ln(NagMount))+50%*(20*ln(MM)+80*ln(MI))
        def powerCmount = parameter.cmount && parameter.cmount > 0 ? (Math.log(parameter.cmount+1)) * 0.1 : 0
        def powerTmount = parameter.tmount && parameter.tmount > 0 ? (Math.log(parameter.tmount+1)) * 0.2 : 0
        def powerSmount = parameter.smount && parameter.smount > 0 ? (Math.log(parameter.smount+1)) * 0.05 : 0
        def powerWmount = parameter.wmount && parameter.wmount > 0 ? (Math.log(parameter.wmount+1)) * 0.05 : 0
        def powerImount = parameter.imount && parameter.imount > 0 ? (Math.log(parameter.imount+1)) * 0.6 : 0
        def powerE = (powerCmount + powerTmount + powerSmount + powerWmount + powerImount) * 0.3

        def powerPmount = parameter.pmount && parameter.pmount > 0 ? (Math.log(parameter.pmount+1)) * 0.4 : 0
        def powerPosMount = parameter.posMount && parameter.posMount > 0 ? (Math.log(parameter.posMount+1)) * 0.6 : 0
        def powerNagMount = parameter.nagMount && parameter.nagMount > 0 ? (Math.log(parameter.nagMount+1)) * 0.4 : 0
        def powerP = (powerPmount + powerPosMount - powerNagMount) * 0.2

        def powerMM = parameter.mM && parameter.mM > 0 ? (Math.log(parameter.mM+1)) * 0.2 : 0
        def powerMI = parameter.mI && parameter.mI > 0 ? (Math.log(parameter.mI+1)) * 0.8 : 0
        def powerM = (powerMM + powerMI) * 0.5

        return powerE + powerP + powerM
    }

    public static void main(String[] args) {

        def start2 = (new Date()).getTime()
        def psi2 = getPSI(["postMount": 5, "reprintMount": 4, "mediaMount": 1305, "mediaImportant": 91])
        println(psi2)
        def mii = getMII(["mM": 66, "mI": 265])
        println(mii)
        def bsi = getBSI(["rpd"     : 2225.25, "rav": 46.7757, "rmax": 100, "rmount": 17795,
                          "zpd"     : 2225.25, "zav": 46.7757, "zmax": 100, "zmount": 17795, "ppd": 1197.625, "pav": 24.97944
                          , "pmax"  : 50, "pmount": 9574, "sPmount": 1197.625, "sPosMount": 1, "sNagMount": 1,
                          "keyMatch": 14, "keyInnovation": 1, "keyPropagation": 1
        ])
        println(bsi)
        def tsi = getTSI(["cmount": 2000, "tmount": 1335, "imount":38,"pmount":1217,"mM":946,"mI":16])
        println(tsi)
        def end2 = (new Date()).getTime()
        println(end2 - start2)
    }
}
