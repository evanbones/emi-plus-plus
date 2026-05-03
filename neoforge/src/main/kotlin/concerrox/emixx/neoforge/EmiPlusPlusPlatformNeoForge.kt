package concerrox.emixx.neoforge

import concerrox.emixx.EmiPlusPlusPlatform
import net.neoforged.fml.loading.FMLPaths
import java.nio.file.Path

object EmiPlusPlusPlatformNeoForge : EmiPlusPlusPlatform {

    override val configDirectoryPath: Path = FMLPaths.CONFIGDIR.get()

}