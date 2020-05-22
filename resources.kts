import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

val resourcesDirectory = Paths.get("out", "production", "resources")
val classesDirectory = Paths.get("out", "production", "classes")

// copy all resources to classes
Files.walk(resourcesDirectory).forEach {
    if (Files.isRegularFile(it)) {
        val target = classesDirectory.resolve(resourcesDirectory.relativize(it))
        target.parent.toFile().mkdirs()
        val lastModified = Files.getLastModifiedTime(it)
        if (Files.notExists(target) || lastModified >= Files.getLastModifiedTime(target)) {
            Files.copy(it, target, StandardCopyOption.REPLACE_EXISTING)
            Files.setLastModifiedTime(target, lastModified)
        }
    }
}
