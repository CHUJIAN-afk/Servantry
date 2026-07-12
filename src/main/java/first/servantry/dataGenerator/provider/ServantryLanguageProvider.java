package first.servantry.dataGenerator.provider;

import first.servantry.register.ServantryLanguageGenerateRegister;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ServantryLanguageProvider extends LanguageProvider {

    private final String locale;

    public ServantryLanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        ServantryLanguageGenerateRegister.init();
        ServantryLanguageGenerateRegister.LanguageGenerate.forEach(langEntry -> langEntry.build(locale, this::add));
    }
}
