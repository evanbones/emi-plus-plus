package concerrox.emixx.forge.mixinplugin

import net.minecraftforge.fml.loading.LoadingModList
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo

class EmiPlusPlusForgeMixinPlugin : IMixinConfigPlugin {

    private val conditionalMixinModIds = listOf("mekanism")

    override fun onLoad(mixinPackage: String) {}

    override fun getRefMapperConfig() = null

    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean {
        conditionalMixinModIds.forEach { modId ->
            if (mixinClassName.contains(modId, ignoreCase = true)) {
                return LoadingModList.get().getModFileById(modId) != null
            }
        }
        return true
    }

    override fun acceptTargets(myTargets: MutableSet<String>, otherTargets: MutableSet<String>) {}

    override fun getMixins() = null

    override fun preApply(
        targetClassName: String, targetClass: ClassNode, mixinClassName: String, mixinInfo: IMixinInfo
    ) {
    }

    override fun postApply(
        targetClassName: String, targetClass: ClassNode, mixinClassName: String, mixinInfo: IMixinInfo
    ) {
    }

}