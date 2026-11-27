package dev.entel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import org.lwjgl.glfw.GLFW;
import java.util.*;
import java.nio.file.*;
public class EntelClient implements ClientModInitializer {
 static final Map<String,Boolean> modules=new LinkedHashMap<>();
 static int key=GLFW.GLFW_KEY_RIGHT_SHIFT;
 static boolean held=false;
 static int ticks=0;
 static final Path config=Path.of("config","entel.properties");
 static final Set<String> reported=new HashSet<>();
 public void onInitializeClient(){
 for(String s:List.of("Sprint","AutoRespawn","BaseFinder")) modules.put(s,false);
 try { Properties p=new Properties(); if(Files.exists(config)){try(var in=Files.newInputStream(config)){p.load(in);} key=Integer.parseInt(p.getProperty("key","344")); modules.replaceAll((n,v)->Boolean.parseBoolean(p.getProperty(n,"false")));}}catch(Exception ignored){}
 ClientTickEvents.END_CLIENT_TICK.register(c->{
 boolean down=GLFW.glfwGetKey(c.getWindow().handle(),key)==GLFW.GLFW_PRESS;
 if(down&&!held&&c.screen==null)c.setScreen(new Menu()); held=down;
 if(c.player==null||c.level==null)return;
 if(modules.get("Sprint")&&c.options.keyUp.isDown()&&!c.player.isShiftKeyDown())c.player.setSprinting(true);
 if(modules.get("AutoRespawn")&&c.player.isDeadOrDying())c.player.respawn();
 if(modules.get("BaseFinder")&&++ticks%20==0){
 BlockPos center=c.player.blockPosition();
 int cx=(center.getX()>>4)<<4, cz=(center.getZ()>>4)<<4;
 int y=c.level.getMinY()+Math.floorMod(ticks/20,Math.max(1,c.level.getHeight()));
 BlockPos.MutableBlockPos pos=new BlockPos.MutableBlockPos();
 for(int x=cx;x<cx+16;x++)for(int z=cz;z<cz+16;z++){
 pos.set(x,y,z); var b=c.level.getBlockState(pos).getBlock();
 if(b==Blocks.CHEST||b==Blocks.BARREL||b==Blocks.CRAFTING_TABLE||b==Blocks.FURNACE||b==Blocks.GLASS||b==Blocks.ENDER_CHEST){
 String loc=x+" "+y+" "+z;
 if(reported.add(loc))c.player.displayClientMessage(Component.literal("[Entel] Suspicious block: "+loc),false);
 }
 }
 }
 });
 }
 static void save(){try{Files.createDirectories(config.getParent());Properties p=new Properties();p.setProperty("key",Integer.toString(key));modules.forEach((n,v)->p.setProperty(n,v.toString()));try(var out=Files.newOutputStream(config)){p.store(out,"Entel Client");}}catch(Exception ignored){}}
 static class Menu extends Screen{
 Menu(){super(Component.literal("Entel Client"));}
 protected void init(){int y=45;for(String n:modules.keySet()){addRenderableWidget(Button.builder(Component.literal(n+": "+(modules.get(n)?"ON":"OFF")),b->{modules.put(n,!modules.get(n));b.setMessage(Component.literal(n+": "+(modules.get(n)?"ON":"OFF")));save();}).bounds(width/2-100,y,200,20).build());y+=24;}
 addRenderableWidget(Button.builder(Component.literal("GUI key: "+key),b->{key=key==344?GLFW.GLFW_KEY_RIGHT_CONTROL:key==345?GLFW.GLFW_KEY_INSERT:344;save();b.setMessage(Component.literal("GUI key: "+key));}).bounds(width/2-100,y,200,20).build());}
 public boolean isPauseScreen(){return false;}
 }
}
