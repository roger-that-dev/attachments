package net.corda.examples.attachments.contract

import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

open class AgreementContract : Contract {
    companion object {
        val AGREEMENT_CONTRACT_ID = "net.corda.examples.attachments.contract.AgreementContract"
    }

    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.
    }

    interface Commands {
        class Agree : TypeOnlyCommandData(), Commands
    }
}