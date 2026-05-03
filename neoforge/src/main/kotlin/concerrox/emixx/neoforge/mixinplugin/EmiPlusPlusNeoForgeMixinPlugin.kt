package concerrox.emixx.neoforge.mixinplugin

import mekanism.api.MekanismAPI
import net.neoforged.fml.loading.LoadingModList
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo

class EmiPlusPlusNeoForgeMixinPlugin : IMixinConfigPlugin {

    private val conditionalMixinModIds = arrayOf(MekanismAPI.MEKANISM_MODID)

    override fun onLoad(mixinPackage: String) {}

    override fun getRefMapperConfig() = null

    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean {
        conditionalMixinModIds.forEach {
            if (mixinClassName.contains(it)) return LoadingModList.get().getModFileById(it) != null
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