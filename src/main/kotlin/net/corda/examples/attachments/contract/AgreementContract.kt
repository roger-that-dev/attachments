package net.corda.examples.attachments.contract

import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractAttachment
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import net.corda.examples.attachments.state.AgreementState
import kotlin.streams.toList


open class AgreementContract : Contract {
    companion object {
        val AGREEMENT_CONTRACT_ID = "net.corda.examples.attachments.contract.AgreementContract"
    }

    override fun verify(tx: LedgerTransaction) = requireThat {
        // Constraints on the inputs and outputs.
        "The transaction should have no inputs" using
                (tx.inputs.isEmpty())
        "The transaction should have an AgreementState output" using
                (tx.outputsOfType<AgreementState>().size == 1)
        "The transaction should have no other outputs" using
                (tx.outputs.size == 1)

        // Constraints on blacklisted parties.
        val nonContractAttachments = tx.attachments.filter { it !is ContractAttachment }
        "The transaction should have a single non-contract attachment" using
                (nonContractAttachments.size == 1)
        val attachmentJar = nonContractAttachments.single().openAsJAR()

        "The attachment jar's first entry should be entitled blacklist.txt" using
                (attachmentJar.nextEntry.name == "blacklist.txt")
        val blacklistedCompanies = attachmentJar.bufferedReader().lines().toList()

        val agreement = tx.outputsOfType<AgreementState>().single()
        val participants = agreement.participants
        val participantsOrgs = participants.map { it.name.organisation }
        "The agreement should not involve any blacklisted parties" using
                (blacklistedCompanies.toSet().intersect(participantsOrgs).isEmpty())
    }

    interface Commands {
        class Agree : TypeOnlyCommandData(), Commands
    }
}