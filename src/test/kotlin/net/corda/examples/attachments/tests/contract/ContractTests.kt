package net.corda.examples.attachments.tests.contract

import net.corda.core.crypto.generateKeyPair
import net.corda.core.identity.CordaX500Name
import net.corda.examples.attachments.contract.AgreementContract
import net.corda.examples.attachments.tests.BLACKLISTED_PARTIES
import net.corda.examples.attachments.BLACKLIST_JAR_PATH
import net.corda.examples.attachments.contract.AgreementContract.Companion.AGREEMENT_CONTRACT_ID
import net.corda.examples.attachments.state.AgreementState
import net.corda.testing.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class ContractTests {
    private val agreementTxt = "${MEGA_CORP.name} agrees with ${MINI_CORP.name} that..."
    private val validAttachment = File(BLACKLIST_JAR_PATH)
    private val blacklistedPartyKeyPair = generateKeyPair()
    private val blacklistedPartyPubKey = blacklistedPartyKeyPair.public
    private val blacklistedPartyName = CordaX500Name(organisation = BLACKLISTED_PARTIES[0], locality = "London", country = "GB")
    private val blacklistedParty = getTestPartyAndCertificate(blacklistedPartyName, blacklistedPartyPubKey).party

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
            val attachmentInputStream = validAttachment.inputStream()
            val attachmentHash = attachment(attachmentInputStream)
            println(attachmentHash)

            transaction {
                output(AGREEMENT_CONTRACT_ID) { AgreementState(MEGA_CORP, MINI_CORP, agreementTxt) }
                command(MEGA_CORP_PUBKEY, MINI_CORP_PUBKEY) { AgreementContract.Commands.Agree() }
                fails()
                attachment(attachmentHash)
                verifies()
            }
        }
    }

    @Test
    fun `the non-contract attachment must not blacklist any of the participants`() {
        ledger {
            // We upload a test attachment to the ledger.
            val attachmentInputStream = validAttachment.inputStream()
            val attachmentHash = attachment(attachmentInputStream)

            transaction {
                output(AGREEMENT_CONTRACT_ID) { AgreementState(MEGA_CORP, blacklistedParty, agreementTxt) }
                command(MEGA_CORP_PUBKEY, blacklistedPartyPubKey) { AgreementContract.Commands.Agree() }
                attachment(attachmentHash)
                fails()
            }
        }
    }
}