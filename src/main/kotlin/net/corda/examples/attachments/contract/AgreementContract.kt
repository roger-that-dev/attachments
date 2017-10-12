package net.corda.examples.attachments.contract

import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractAttachment
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

open class AgreementContract : Contract {
    companion object {
        val AGREEMENT_CONTRACT_ID = "net.corda.examples.attachments.contract.AgreementContract"
    }

    override fun verify(tx: LedgerTransaction) = requireThat {
        val nonContractAttachments = tx.attachments.filter { it !is ContractAttachment }
        "There is a single non-contract attachment" using
                (nonContractAttachments.size == 1)
        val attachment = nonContractAttachments.single()
    }

    interface Commands {
        class Agree : TypeOnlyCommandData(), Commands
    }
}