package me.tatarka.holdr.intellij.plugin;

import com.android.tools.idea.gradle.AndroidProjectKeys;
import com.google.common.collect.Maps;
import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.Key;
import com.intellij.openapi.externalSystem.service.project.manage.ProjectDataService;
import com.intellij.openapi.externalSystem.util.DisposeAwareProjectChange;
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import me.tatarka.holdr.model.HoldrCompiler;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

/**
 * Created by evan on 9/28/14.
 */
public class HoldrDataService implements ProjectDataService<HoldrData, HoldrData> {
    public static final Key<HoldrData> HOLDR_CONFIG_KEY = Key.create(HoldrData.class, AndroidProjectKeys.IDE_ANDROID_PROJECT.getProcessingWeight() + 9);

    @NotNull
    @Override
    public Key<HoldrData> getTargetDataKey() {
        return HOLDR_CONFIG_KEY;
    }

    @Override
    public void importData(@NotNull final Collection<DataNode<HoldrData>> toImport, @NotNull final Project project, boolean synchronous) {
        if (!toImport.isEmpty()) {
            ExternalSystemApiUtil.executeProjectChangeAction(synchronous, new DisposeAwareProjectChange(project) {
                @Override
                public void execute() {
                    Map<String, HoldrCompiler> holdrCompilerMap = indexByModuleName(toImport);
                    ModuleManager moduleManager = ModuleManager.getInstance(project);

                    for (Module module : moduleManager.getModules()) {
                        HoldrCompiler holdrCompiler = holdrCompilerMap.get(module.getName());
                        if (holdrCompiler != null) {
                            HoldrModel.put(module, holdrCompiler);
                        } else {
                            HoldrModel.delete(module);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void removeData(@NotNull final Collection<? extends HoldrData> toRemove, @NotNull final Project project, boolean synchronous) {
        if (!toRemove.isEmpty()) {
            ExternalSystemApiUtil.executeProjectChangeAction(synchronous, new DisposeAwareProjectChange(project) {
                @Override
                public void execute() {
                    Map<String, HoldrCompiler> holdrCompilerMap = indexRemoveByModuleName(toRemove);
                    ModuleManager moduleManager = ModuleManager.getInstance(project);

                    for (Module module : moduleManager.getModules()) {
                        HoldrCompiler holdrCompiler = holdrCompilerMap.get(module.getName());
                        if (holdrCompiler != null) {
                            HoldrModel.delete(module);
                        }
                    }
                }
            });
        }
    }

    @NotNull
    private static Map<String, HoldrCompiler> indexByModuleName(@NotNull Collection<DataNode<HoldrData>> dataNodes) {
        Map<String, HoldrCompiler> index = Maps.newHashMap();
        for (DataNode<HoldrData> d : dataNodes) {
            HoldrData data = d.getData();
            index.put(data.getModuleName(), data.getCompiler());
        }
        return index;
    }

    @NotNull
    private static Map<String, HoldrCompiler> indexRemoveByModuleName(@NotNull Collection<? extends HoldrData> dataNodes) {
        Map<String, HoldrCompiler> index = Maps.newHashMap();
        for (HoldrData data : dataNodes) {
            index.put(data.getModuleName(), data.getCompiler());
        }
        return index;
    }
}
