package net.corda.examples.attachments.api

import net.corda.core.contracts.AttachmentResolutionException
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import net.corda.examples.attachments.contract.AgreementContract.Companion.BLACKLIST_JAR_HASH
import net.corda.examples.attachments.flow.ProposeFlow
import net.corda.examples.attachments.state.AgreementState
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST
import javax.ws.rs.core.Response.Status.CREATED

// This API is accessible from /api/agreement. All paths specified below are relative to it.
@Path("a")
class AgreementApi(private val rpcOps: CordaRPCOps) {
    companion object {
        private val logger = loggerFor<AgreementApi>()
    }

    @GET
    @Path("agreements")
    @Produces(MediaType.APPLICATION_JSON)
    fun agreements() = rpcOps.vaultQueryBy<AgreementState>().states.map { it.state.data }

    @GET
    @Path("propose-agreement")
    fun proposeAgreement(
            @QueryParam("counterparty") counterpartyName: CordaX500Name?,
            @QueryParam("agreement") agreementTxt: String): Response {

        if (counterpartyName == null) return Response
                .status(BAD_REQUEST)
                .entity("Query parameter 'counterparty' missing or has wrong format.\n")
                .build()

        val counterparty = rpcOps.wellKnownPartyFromX500Name(counterpartyName) ?: return Response
                .status(BAD_REQUEST)
                .entity("Party $counterpartyName cannot be found on the network.\n")
                .build()

        return try {
            rpcOps.startFlow(::ProposeFlow, agreementTxt, BLACKLIST_JAR_HASH, counterparty).returnValue.getOrThrow()
            Response.status(CREATED).entity("Agreement reached.\n").build()

        } catch (ex: AttachmentResolutionException) {
            // TODO: Explain how to upload the blacklist in the error message.
            val msg = "You must upload the jar containing the blacklisted parties first."
            Response.status(BAD_REQUEST).entity(msg).build()

        } catch (ex: Throwable) {
            Response.status(BAD_REQUEST).entity(ex.message).build()
        }
    }
}