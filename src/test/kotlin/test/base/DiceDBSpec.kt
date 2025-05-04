package test.base

import dicedb.client.Client
import io.kotest.core.annotation.Ignored
import io.kotest.core.listeners.AfterSpecListener
import io.kotest.core.listeners.BeforeSpecListener
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.BehaviorSpec
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

@Ignored
open class DiceDBSpec : BehaviorSpec(), BeforeSpecListener, AfterSpecListener {
    private val container: GenericContainer<*> =
        GenericContainer(DockerImageName.parse("dicedb/dicedb:latest")).withExposedPorts(7379)

    protected lateinit var client: Client

    override suspend fun beforeSpec(spec: Spec) {
        container.start()
        client = Client(container.host, container.getMappedPort(7379))
    }

    override suspend fun afterSpec(spec: Spec) {
        container.stop()
        client.close()
    }
}
