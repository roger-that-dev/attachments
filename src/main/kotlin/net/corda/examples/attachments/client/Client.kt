package net.corda.examples.attachments.client

import net.corda.client.rpc.CordaRPCClient
import net.corda.core.utilities.NetworkHostAndPort.Companion.parse
import net.corda.core.utilities.loggerFor
import net.corda.examples.attachments.BLACKLIST_JAR_PATH
import org.slf4j.Logger
import java.io.File

/**
 * Uploads the jar of blacklisted counterparties with whom agreements cannot be struck to the node.
 */
fun main(args: Array<String>) {
    UploadBlacklistClient().main(args)
}

private class UploadBlacklistClient {
    companion object {
        val logger: Logger = loggerFor<UploadBlacklistClient>()
    }

    fun main(args: Array<String>) {
        require(args.isNotEmpty()) { "Usage: uploadBlacklist <node address>" }
        args.forEach { arg ->
            val nodeAddress = parse(arg)
            val rpcConnection = CordaRPCClient(nodeAddress).start("user1", "test")
            val proxy = rpcConnection.proxy

            val attachmentInputStream = File(BLACKLIST_JAR_PATH).inputStream()
            proxy.uploadAttachment(attachmentInputStream)

            logger.info("Blacklist uploaded to node at $nodeAddress")

            rpcConnection.notifyServerAndClose()
        }
    }
}