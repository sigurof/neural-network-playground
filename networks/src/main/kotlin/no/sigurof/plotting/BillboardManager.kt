package no.sigurof.plotting

import no.sigurof.plotting.BillboardResource
import no.sigurof.plotting.Loader


object BillboardManager {

    fun getBillboardResource(): BillboardResource {
        return BillboardResource(
            Loader.createVao(),
            4
        )
    }
}
