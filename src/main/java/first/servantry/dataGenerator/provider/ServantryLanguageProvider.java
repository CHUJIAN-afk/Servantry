package first.servantry.dataGenerator.provider;

import first.lyra.register.LyraLanguageRegister;
import first.servantry.register.ServantryLanguageGenerateRegister;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import oshi.util.tuples.Pair;

public class ServantryLanguageProvider extends LanguageProvider {

    private final String locale;

    public ServantryLanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        // 主 mod 静态语言键
        ServantryLanguageGenerateRegister.init();
        ServantryLanguageGenerateRegister.LanguageGenerate.entrySet()
                .removeIf(entry -> {
                    String key = entry.getKey();
                    Pair<String, String> value = entry.getValue();
                    String enDesc = value.getA();
                    String zhDesc = value.getB();
                    if (key != null) {
                        if (enDesc != null && "en_us".equals(locale)) {
                            add(key, enDesc);
                        }
                        if (zhDesc != null && "zh_cn".equals(locale)) {
                            add(key, zhDesc);
                            return true;
                        }
                    }
                    return false;
                });
        // LyraItemRegisterBuilder 收集的物品语言键(仅输出 servantry 命名空间,库键由 Lyra 自己生成)
        LyraLanguageRegister.LanguageGenerate.entrySet()
                .removeIf(entry -> {
                    String key = entry.getKey();
                    String[] value = entry.getValue();
                    if (key != null && key.contains("servantry")) {
                        String enDesc = value[0];
                        String zhDesc = value[1];
                        if (enDesc != null && "en_us".equals(locale)) {
                            add(key, enDesc);
                        }
                        if (zhDesc != null && "zh_cn".equals(locale)) {
                            add(key, zhDesc);
                            return true;
                        }
                    }
                    return false;
                });
    }
}
