package org.example


object BillboardManager {

    fun getBillboardResource(): BillboardResource {
        return BillboardResource(
            Loader.createVao(),
            4
        )
    }
}
