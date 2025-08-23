package io.github.rascmatt.z3

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory
import javax.inject.Inject

abstract class Z3Extension @Inject constructor(objects: ObjectFactory, private val providers: ProviderFactory) {

    val bundleCoordinates = objects.property(String::class.java)
        .convention(
            providers.provider {
                System.getProperty("z3.bundleCoords")  // optional JVM override
                    ?: System.getenv("Z3_BUNDLE_COORDS") // optional env override
            }
        )

    val version = objects.property(String::class.java)
        .convention(
            providers.provider {
                System.getProperty("z3.version")  // optional JVM override
                    ?: System.getenv("Z3_VERSION") // optional env override
            }
        )
}