package com.metricv.mirai.flatterbot;

import com.metricv.mirai.matcher.AtMatcher;
import com.metricv.mirai.matcher.PrefixMatcher;
import com.metricv.mirai.matcher.PsuedoMatcher;
import com.metricv.mirai.matcher.RegexMatcher;
import com.metricv.mirai.router.*;
import net.mamoe.mirai.console.plugins.Config;
import net.mamoe.mirai.console.plugins.PluginBase;
import net.mamoe.mirai.message.data.*;
import org.intellij.lang.annotations.RegExp;

import java.io.*;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipInputStream;

class App extends PluginBase {

    List<String> finalTriggers = new ArrayList<>();
    List<String> finalTemplates = new ArrayList<>();

    public void onLoad() {
        if(! new File(getDataFolder().getAbsolutePath() + File.separator + "formula.yml").exists()) {
            File templateFile = new File(getDataFolder().getAbsolutePath() + File.separator + "formula.yml");
            String jarFile = App.class.getProtectionDomain().getCodeSource().getLocation().getPath()
                    .replace("%20", " ");
            try {
                JarFile jarFile1 = new JarFile(new File(jarFile));
                InputStream tmplateIn = jarFile1.getInputStream(jarFile1.getEntry("formula.yml"));
                templateFile.createNewFile();
                FileOutputStream fout = new FileOutputStream(templateFile);
                while(tmplateIn.available()>0) fout.write(tmplateIn.read());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        getLogger().info("Plugin loaded!");
    }

    public void onEnable() {

        Config myConfig = this.loadConfig("formula.yml");

        List<String> triggers = new ArrayList<String>();
        triggers.add("来个(?<name>.+)笑话");
        triggers.add("(?<name>.+)太强了");
        List<String> templates = new ArrayList<String>();
        templates.add("Erdos相信上帝有一本记录所有数学中绝妙证明的书，上帝相信这本书在%name%手里");

        myConfig.setIfAbsent("trigger", triggers);
        myConfig.setIfAbsent("template",templates);

        finalTriggers = myConfig.getStringList("trigger");
        finalTemplates = myConfig.getStringList("template");

        myConfig.save();

        ComplexRouting crtest = ComplexRouting.withBlankRoot();
        ComplexRouting.MatcherNode opt_me = crtest.makeNode("At", new AtMatcher(-2));
        ComplexRouting.MatcherNode final_node = crtest.makeNode("final", new PsuedoMatcher());

        for(@RegExp String trig: finalTriggers) {
            ComplexRouting.MatcherNode route = crtest.makeNode("body", new RegexMatcher(trig));
            opt_me.addChild(route);
            crtest.getRoot().addChild(route);
            route.addChild(final_node);
        }

        final_node.setTerminate().setTarget(this::flatter);

        Routing AttedOne = SimpleRouting.serialRoute()
                .thenMatch("at", new AtMatcher(-1))
                .thenMatch(new PrefixMatcher("太强了"))
                .setTarget(this::flatterAt);
        Routing Attedtwo = SimpleRouting.serialRoute()
                .thenMatch(new RegexMatcher("来个"))
                .thenMatch("at", new AtMatcher(-1))
                .thenMatch(new PrefixMatcher("笑话"))
                .setTarget(this::flatterAt);

        Router.getInstance().addGroupRouting(crtest);
        Router.getInstance().addFriendRouting(crtest);
        Router.getInstance().addTempRouting(crtest);
        Router.getInstance().addGroupRouting(AttedOne);
        Router.getInstance().addGroupRouting(Attedtwo);
    }

    private void flatter(RoutingResult rr) {
        Random newRnd = new Random(System.currentTimeMillis());
        int rndIndex = newRnd.nextInt(finalTemplates.size());
        java.util.regex.Matcher matcher = (java.util.regex.Matcher) rr.get("body");
        rr.sendMessage(finalTemplates.get(rndIndex).replaceAll("%name%", matcher.group("name")));
    }

    private void flatterAt(RoutingResult rr) {
        Random newRnd = new Random(System.currentTimeMillis());
        int rndIndex = newRnd.nextInt(finalTemplates.size());
        System.out.println(rr.keySet().toString());
        At atted = (At)rr.get("at");
        rr.sendMessage(finalTemplates.get(rndIndex).replaceAll("%name%", atted.getDisplay()));
    }
}          