package dev.entel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
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
 static KeyMapping guiKey;
 static boolean held=false;
 static int ticks=0;
 static final Path config=Path.of("config","entel.properties");
 static final Set<String> reported=new HashSet<>();
 public void onInitializeClient(){
 guiKey=KeyBindingHelper.registerKeyBinding(new KeyMapping("key.entelclient.gui",GLFW.GLFW_KEY_RIGHT_SHIFT,KeyMapping.Category.register(Identifier.fromNamespaceAndPath("entelclient","controls"))));
 for(String s:List.of("Sprint","AutoRespawn","BaseFinder","Fullbright","AutoJump","SafeWalk","AutoAttack","AutoUse","Coordinates","AntiAFK")) modules.put(s,false);
 try { Properties p=new Properties(); if(Files.exists(config)){try(var in=Files.newInputStream(config)){p.load(in);} modules.replaceAll((n,v)->Boolean.parseBoolean(p.getProperty(n,"false")));}}catch(Exception ignored){}
 ClientTickEvents.END_CLIENT_TICK.register(c->{
 while(guiKey.consumeClick())if(c.screen==null)c.setScreen(new Menu());
 if(c.player==null||c.level==null)return;
 if(modules.get("Sprint")&&c.options.keyUp.isDown()&&!c.player.isShiftKeyDown())c.player.setSprinting(true);
 if(modules.get("AutoRespawn")&&c.player.isDeadOrDying())c.player.respawn();
 if(modules.get("Fullbright"))c.player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION,300,0,false,false,false));
 if(c.screen==null){
 if(modules.get("AutoJump")&&c.player.onGround()&&c.options.keyUp.isDown())c.player.jumpFromGround();
 if(modules.get("SafeWalk")&&c.player.onGround()&&c.level.isEmptyBlock(c.player.blockPosition().below()))c.player.setShiftKeyDown(true);
 if(modules.get("AutoAttack")&&c.crosshairPickEntity!=null&&c.player.getAttackStrengthScale(0)>=1&&c.gameMode!=null){c.gameMode.attack(c.player,c.crosshairPickEntity);c.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);}
 if(modules.get("AutoUse")&&c.gameMode!=null&&c.player.getMainHandItem().getItem() instanceof net.minecraft.world.item.BlockItem&&c.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit&&c.player.tickCount%4==0)c.gameMode.useItemOn(c.player,net.minecraft.world.InteractionHand.MAIN_HAND,hit);
 if(modules.get("AntiAFK")&&c.player.tickCount%600==0&&c.player.onGround())c.player.jumpFromGround();
 }
 if(modules.get("Coordinates")&&c.player.tickCount%20==0)c.player.displayClientMessage(Component.literal(c.player.blockPosition().toShortString()),true);
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
 static void save(){try{Files.createDirectories(config.getParent());Properties p=new Properties();modules.forEach((n,v)->p.setProperty(n,v.toString()));try(var out=Files.newOutputStream(config)){p.store(out,"Entel Client");}}catch(Exception ignored){}}
 static class Menu extends Screen{
 final String[][] groups={{"Combat","AutoAttack"},{"Movement","Sprint","AutoJump","SafeWalk"},{"Render","Fullbright","Coordinates"},{"Player","AutoRespawn","AutoUse","AntiAFK"},{"World","BaseFinder"}};
 Menu(){super(Component.literal("Entel Client"));}
 protected void init(){
 int cols=width>=620?5:width>=360?3:2;
 int w=Math.min(118,(width-20)/cols-6);
 for(int i=0;i<groups.length;i++){int x=(width-cols*(w+6))/2+(i%cols)*(w+6);int y=40+(i/cols)*110;
 for(int j=1;j<groups[i].length;j++){String n=groups[i][j];addRenderableWidget(Button.builder(label(n),b->{modules.put(n,!modules.get(n));b.setMessage(label(n));save();}).bounds(x+3,y+20+(j-1)*20,w-6,19).build());}}
 }
 Component label(String n){return Component.literal((modules.get(n)?"● ":"○ ")+n).withColor(modules.get(n)?0xC4A5FF:0xB0B0BE);}
 public void render(GuiGraphics g,int mx,int my,float delta){
 g.fill(0,0,width,height,0xE6101018);
 g.drawCenteredString(font,"ENTEL CLIENT",width/2,12,0xC4A5FF);
 int cols=width>=620?5:width>=360?3:2;int w=Math.min(118,(width-20)/cols-6);
 for(int i=0;i<groups.length;i++){int x=(width-cols*(w+6))/2+(i%cols)*(w+6);int y=40+(i/cols)*110;g.fill(x,y,x+w,y+20+20*(groups[i].length-1)+4,0xFF20202D);g.fill(x,y,x+w,y+17,0xFF6845A5);g.drawCenteredString(font,groups[i][0],x+w/2,y+5,0xFFFFFFFF);}
 super.render(g,mx,my,delta);
 }
 public boolean isPauseScreen(){return false;}
 }
}
