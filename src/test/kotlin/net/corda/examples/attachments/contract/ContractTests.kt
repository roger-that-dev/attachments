package net.corda.examples.attachments.contract

import net.corda.core.crypto.SecureHash.Companion.zeroHash
import net.corda.examples.attachments.ATTACHMENT_JAR_PATH
import net.corda.examples.attachments.contract.AgreementContract.Companion.AGREEMENT_CONTRACT_ID
import net.corda.examples.attachments.state.AgreementState
import net.corda.testing.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class ContractTests {
    private val agreementTxt = "${MEGA_CORP.name} agrees with ${MINI_CORP.name} that..."

    @Before
    fun setup() {
        setCordappPackages("net.corda.examples.attachments.contract")
    }

    @After
    fun tearDown() {
        unsetCordappPackages()
    }

    @Test
    fun `agreement transaction contains one non-contract attachment`() {
        ledger {
            // We upload a test attachment to the ledger.
            val attachmentInputStream = File(ATTACHMENT_JAR_PATH).inputStream()
            val attachmentHash = attachment(attachmentInputStream)

            transaction {
                output(AGREEMENT_CONTRACT_ID) { AgreementState(MEGA_CORP, MINI_CORP, agreementTxt) }
                command(MEGA_CORP_PUBKEY, MINI_CORP_PUBKEY) { AgreementContract.Commands.Agree() }
                fails()
                attachment(attachmentHash)
                verifies()
            }
        }
    }
}