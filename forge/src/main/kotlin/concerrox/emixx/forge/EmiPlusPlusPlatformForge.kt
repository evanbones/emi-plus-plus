package concerrox.emixx.forge

import concerrox.emixx.EmiPlusPlusPlatform
import net.minecraftforge.fml.loading.FMLPaths
import java.nio.file.Path

object EmiPlusPlusPlatformForge : EmiPlusPlusPlatform {

    override val configDirectoryPath: Path = FMLPaths.CONFIGDIR.get()

}