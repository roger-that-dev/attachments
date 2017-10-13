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
        require(args.size == 1) { "Usage: uploadBlacklist <node address>" }
        val nodeAddress = parse(args[0])
        val client = CordaRPCClient(nodeAddress)
        val proxy = client.start("user1", "test").proxy

        val attachmentInputStream = File(BLACKLIST_JAR_PATH).inputStream()
        proxy.uploadAttachment(attachmentInputStream)

        logger.info("Blacklist uploaded to node via $nodeAddress.")
    }
}