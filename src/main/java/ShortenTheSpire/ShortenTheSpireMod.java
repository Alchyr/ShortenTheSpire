package ShortenTheSpire;

import ShortenTheSpire.Util.KeywordWithProper;
import ShortenTheSpire.Util.ReplaceData;
import basemod.BaseMod;
import basemod.ReflectionHacks;
import basemod.interfaces.EditKeywordsSubscriber;
import basemod.interfaces.EditStringsSubscriber;
import com.badlogic.gdx.Gdx;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.Gson;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.megacrit.cardcrawl.core.Settings.GameLanguage.ENG;

@SpireInitializer
public class ShortenTheSpireMod implements EditStringsSubscriber, EditKeywordsSubscriber {
    public static final Logger logger = LogManager.getLogger(ShortenTheSpireMod.class.getSimpleName());

    public static Settings.GameLanguage[] SupportedLanguages = {
            ENG
    };

    private static ReplaceData[] cardWords;
    private static ReplaceData[] eventDescriptionWords;
    private static ReplaceData[] eventOptionWords;


    public static String assetPath(String partialPath)
    {
        return "ShortenTheSpireMod/" + partialPath;
    }

    @SuppressWarnings("unused")
    public static void initialize() {
        new ShortenTheSpireMod();
    }

    public ShortenTheSpireMod()
    {
        BaseMod.subscribe(this);
    }

    @Override
    public void receiveEditStrings() {
        //Load important words

        try
        {
            String lang = getLangString();

            Gson gson = new Gson();
            String json = Gdx.files.internal(assetPath("localization/" + lang + "/CardImportant.json")).readString(String.valueOf(StandardCharsets.UTF_8));
            cardWords = gson.fromJson(json, ReplaceData[].class);

            json = Gdx.files.internal(assetPath("localization/" + lang + "/EventDescriptionImportant.json")).readString(String.valueOf(StandardCharsets.UTF_8));
            eventDescriptionWords = gson.fromJson(json, ReplaceData[].class);

            json = Gdx.files.internal(assetPath("localization/" + lang + "/EventOptionImportant.json")).readString(String.valueOf(StandardCharsets.UTF_8));
            eventOptionWords = gson.fromJson(json, ReplaceData[].class);
        }
        catch (Exception e)
        {
            logger.error("Failed to load important strings.");
        }

        //Load pre-defined modified strings
    }

    @Override
    public void receiveEditKeywords()
    {
        String lang = getLangString();

        logger.info("Adding better keywords.");

        Gson gson = new Gson();
        String json = Gdx.files.internal(assetPath("localization/" + lang + "/Keywords.json")).readString(String.valueOf(StandardCharsets.UTF_8));
        KeywordWithProper[] keywords = gson.fromJson(json, KeywordWithProper[].class);

        if (keywords != null) {
            for (KeywordWithProper keyword : keywords) {
                BaseMod.addKeyword(keyword.PROPER_NAME, keyword.NAMES, keyword.DESCRIPTION);
            }
        }
    }

    public static void PostLoadLocalizationStrings(LocalizedStrings localizedStrings)
    {
        logger.info("Improving strings.");
        switch (Settings.language)
        {
            case ENG:
                EnglishImproveStrings(localizedStrings);
                break;
            default:
                //Nothing at all. This is kind of unnecessary but I put it in anyways.
                break;
        }
        logger.info("Yes");
    }


    private String getLangString()
    {
        for (Settings.GameLanguage lang : SupportedLanguages)
        {
            if (lang.equals(Settings.language))
            {
                return Settings.language.name().toLowerCase();
            }
        }
        return "eng";
    }

    @SuppressWarnings("unchecked")
    private static void EnglishImproveStrings(LocalizedStrings localizedStrings)
    {
        try
        {
            Map<String, CardStrings> cardStrings = (Map<String, CardStrings>)ReflectionHacks.getPrivateStatic(LocalizedStrings.class, "cards");
            if (cardStrings != null)
            {
                for (CardStrings cardString : cardStrings.values())
                {
                    EnglishHeckStrings(cardString);
                }

                ReflectionHacks.setPrivateStaticFinal(LocalizedStrings.class, "cards", cardStrings);
            }

            /*
            Map<String, EventStrings> eventStrings = (Map<String, EventStrings>)ReflectionHacks.getPrivateStatic(LocalizedStrings.class, "events");
            if (eventStrings != null)
            {
                for (EventStrings eventString : eventStrings.values())
                {
                    EnglishHeckStrings(eventString);
                }

                ReflectionHacks.setPrivateStaticFinal(LocalizedStrings.class, "events", eventStrings);
            }*/
        }
        catch (Exception e)
        {
            logger.error("Error while hecking strings - " + e.getMessage());
        }



        /*Map<String, RelicStrings> relicStrings = (Map<String, RelicStrings>)ReflectionHacks.getPrivateStatic(LocalizedStrings.class, "relics");
        if (relicStrings != null)
        {
            for (RelicStrings relicString : relicStrings.values())
            {
                EnglishHeckStrings(relicString);
            }

            ReflectionHacks.setPrivateStaticFinal(LocalizedStrings.class, "relics", relicStrings);
        }*/
    }
    private static void EnglishHeckStrings(CardStrings cardStrings)
    {
        if (cardStrings.DESCRIPTION != null)
            cardStrings.DESCRIPTION = EnglishDestroyCardString(cardStrings.DESCRIPTION);

        if (cardStrings.UPGRADE_DESCRIPTION != null)
            cardStrings.UPGRADE_DESCRIPTION = EnglishDestroyCardString(cardStrings.UPGRADE_DESCRIPTION);

        if (cardStrings.EXTENDED_DESCRIPTION != null)
            for (int i = 0; i < cardStrings.EXTENDED_DESCRIPTION.length; i++)
                cardStrings.EXTENDED_DESCRIPTION[i] = EnglishDestroyCardString(cardStrings.EXTENDED_DESCRIPTION[i]);
    }
    private static void EnglishHeckStrings(EventStrings eventStrings)
    {
        if (eventStrings.DESCRIPTIONS != null)
            for (int i = 0; i < eventStrings.DESCRIPTIONS.length; i++)
                eventStrings.DESCRIPTIONS[i] = EnglishDestroyString(eventStrings.DESCRIPTIONS[i], eventDescriptionWords);

        if (eventStrings.OPTIONS != null)
            for (int i = 0; i < eventStrings.OPTIONS.length; i++)
                eventStrings.OPTIONS[i] = EnglishDestroyString(eventStrings.OPTIONS[i], eventOptionWords);
    }
    private static String EnglishDestroyCardString(String spireString)
    {
        String returnString = spireString;
        for (ReplaceData data : cardWords)
        {
            for (String phrase : data.KEYS)
            {
                if (data.VALUE == null)
                {
                    data.VALUE = "";
                }
                String replacement = returnString.replaceAll(phrase, data.VALUE);
                if (replacement.contains("ShortenTheSpireSpecialValue"))
                {
                    Matcher matches = Pattern.compile(phrase).matcher(returnString);
                    while (matches.find())
                    {
                        replacement = replacement.replaceFirst("ShortenTheSpireSpecialValue", matches.group(data.SPECIAL));
                    }
                }
                if (replacement.contains("ShortenTheSpireMix"))
                {
                    Matcher matches = Pattern.compile(phrase).matcher(returnString);
                    if (matches.find())
                    {
                        String replacementReplacement = "";
                        for (int i : data.REORGANIZE)
                        {
                            replacementReplacement = replacementReplacement.concat(matches.group(i));
                        }
                        replacement = replacement.replace("ShortenTheSpireMix", replacementReplacement);
                    }
                }
                if (replacement.contains("ShortenTheSpireCapitalize"))
                {
                    Matcher matches = Pattern.compile(phrase).matcher(returnString);
                    if (matches.find())
                    {
                        replacement = replacement.replace("ShortenTheSpireCapitalize", matches.group(1).toUpperCase());
                    }
                }
                returnString = replacement;
            }
        }

        return returnString;
    }

    private static String EnglishDestroyString(String spireString, ReplaceData[] replacementData)
    {
        String returnString = spireString;

        for (ReplaceData data : replacementData)
        {
            for (String phrase : data.KEYS)
            {
                if (data.VALUE == null)
                {
                    data.VALUE = "";
                }
                String replacement = returnString.replaceAll(phrase, data.VALUE);
                if (replacement.contains("ShortenTheSpireSpecialValue"))
                {
                    Matcher matches = Pattern.compile(phrase).matcher(returnString);
                    while (matches.find())
                    {
                        replacement = replacement.replaceFirst("ShortenTheSpireSpecialValue", matches.group(data.SPECIAL));
                    }
                }
                returnString = replacement;
            }
        }

        return returnString;
    }
}
