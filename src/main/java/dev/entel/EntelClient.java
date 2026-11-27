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
 for(String s:List.of("Sprint","AutoRespawn","BaseFinder","Fullbright","AutoJump","SafeWalk","AutoAttack","AutoUse","Coordinates","AntiAFK","AutoSneak","NoJumpDelay","AutoSwim","SpeedHUD","DirectionHUD","HealthHUD","AutoTotem","Scaffold")) modules.put(s,false);
 try { Properties p=new Properties(); if(Files.exists(config)){try(var in=Files.newInputStream(config)){p.load(in);} modules.replaceAll((n,v)->Boolean.parseBoolean(p.getProperty(n,"false")));}}catch(Exception ignored){}
 ClientTickEvents.END_CLIENT_TICK.register(c->{
 while(guiKey.consumeClick())if(c.screen==null)c.setScreen(new Menu());
 if(c.player==null||c.level==null)return;
 if(modules.get("Sprint")&&c.options.keyUp.isDown()&&!c.player.isShiftKeyDown())c.player.setSprinting(true);
 if(modules.get("AutoRespawn")&&c.player.isDeadOrDying())c.player.respawn();
 if(modules.get("Fullbright"))c.player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION,300,0,false,false,false));
 if(c.screen==null){
 if(modules.get("Scaffold")) scaffold(c);
 if(modules.get("AutoJump")&&c.player.onGround()&&c.options.keyUp.isDown())c.player.jumpFromGround();
 if(modules.get("SafeWalk")&&c.player.onGround()&&c.level.isEmptyBlock(BlockPos.containing(c.player.getX()+c.player.getDeltaMovement().x*2,c.player.getY()-0.5,c.player.getZ()+c.player.getDeltaMovement().z*2)))c.player.setShiftKeyDown(true);
 if(modules.get("AutoAttack")&&c.crosshairPickEntity!=null&&c.player.getAttackStrengthScale(0)>=1&&c.gameMode!=null){c.gameMode.attack(c.player,c.crosshairPickEntity);c.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);}
 if(modules.get("AutoUse")&&c.gameMode!=null&&c.player.getMainHandItem().getItem() instanceof net.minecraft.world.item.BlockItem&&c.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit&&c.player.tickCount%4==0)c.gameMode.useItemOn(c.player,net.minecraft.world.InteractionHand.MAIN_HAND,hit);
 if(modules.get("AntiAFK")&&c.player.tickCount%600==0&&c.player.onGround())c.player.jumpFromGround();
 }
 if(modules.get("AutoSneak"))c.player.setShiftKeyDown(true);
 if(modules.get("AutoSwim")&&c.player.isInWater())c.player.setSprinting(true);
 if(modules.get("NoJumpDelay")&&c.player.onGround()&&c.options.keyJump.isDown())c.player.jumpFromGround();
 if(modules.get("AutoTotem")&&c.gameMode!=null&&c.player.getOffhandItem().isEmpty()){
 for(int i=0;i<36;i++)if(c.player.getInventory().getItem(i).is(net.minecraft.world.item.Items.TOTEM_OF_UNDYING)){
 c.gameMode.handleInventoryMouseClick(c.player.inventoryMenu.containerId,i<9?36+i:i,40,net.minecraft.world.inventory.ClickType.SWAP,c.player);break;}
 }
 StringBuilder hud=new StringBuilder();
 if(modules.get("Coordinates"))hud.append(c.player.blockPosition().toShortString());
 if(modules.get("SpeedHUD"))hud.append(String.format(java.util.Locale.ROOT," | %.1f b/s",c.player.getDeltaMovement().horizontalDistance()*20));
 if(modules.get("DirectionHUD"))hud.append(" | ").append(c.player.getDirection().getName());
 if(modules.get("HealthHUD"))hud.append(" | HP ").append((int)c.player.getHealth());
 if(!hud.isEmpty())c.player.displayClientMessage(Component.literal(hud.toString()),true);
 if(modules.get("BaseFinder")&&++ticks%1==0){
 BlockPos center=c.player.blockPosition();
 int cx=(center.getX()>>4)<<4, cz=(center.getZ()>>4)<<4;
 int y=c.level.getMinY()+Math.floorMod(ticks,Math.max(1,c.level.getHeight()));
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
 static void scaffold(Minecraft c){
 if(c.gameMode==null||c.player==null||c.level==null)return;
 if(!c.options.keyUp.isDown()&&!c.options.keyDown.isDown()&&!c.options.keyLeft.isDown()&&!c.options.keyRight.isDown())return;
 BlockPos target=BlockPos.containing(c.player.getX(),c.player.getY()-0.5,c.player.getZ());
 if(!c.level.getBlockState(target).canBeReplaced())return;
 var hand=net.minecraft.world.InteractionHand.MAIN_HAND;
 int old=c.player.getInventory().getSelectedSlot(), slot=-1;
 if(c.player.getOffhandItem().getItem() instanceof net.minecraft.world.item.BlockItem)hand=net.minecraft.world.InteractionHand.OFF_HAND;
 else {for(int i=0;i<9;i++)if(c.player.getInventory().getItem(i).getItem() instanceof net.minecraft.world.item.BlockItem){slot=i;break;}if(slot<0)return;}
 for(var dir:net.minecraft.core.Direction.values()){
 BlockPos support=target.relative(dir);
 if(c.level.getBlockState(support).canBeReplaced())continue;
 var face=dir.getOpposite();
 var hit=net.minecraft.world.phys.Vec3.atCenterOf(support).add(face.getStepX()*0.5,face.getStepY()*0.5,face.getStepZ()*0.5);
 if(slot>=0)c.player.getInventory().setSelectedSlot(slot);
 try{var result=c.gameMode.useItemOn(c.player,hand,new net.minecraft.world.phys.BlockHitResult(hit,face,support,false));if(result.consumesAction())c.player.swing(hand);}finally{if(slot>=0)c.player.getInventory().setSelectedSlot(old);}
 break;
 }
 }
 static void save(){try{Files.createDirectories(config.getParent());Properties p=new Properties();modules.forEach((n,v)->p.setProperty(n,v.toString()));try(var out=Files.newOutputStream(config)){p.store(out,"Entel Client");}}catch(Exception ignored){}}
 static class Menu extends Screen{
 final String[][] groups={{"Combat","AutoAttack","AutoTotem"},{"Movement","Sprint","AutoJump","SafeWalk","AutoSneak","NoJumpDelay","AutoSwim"},{"Render","Fullbright","Coordinates","SpeedHUD","DirectionHUD","HealthHUD"},{"Player","AutoRespawn","AutoUse","AntiAFK"},{"World","BaseFinder","Scaffold"}};
 Menu(){super(Component.literal("Entel Client"));}
 protected void init(){
 int cols=width>=620?5:width>=360?3:2;
 int w=Math.min(118,(width-20)/cols-6);
 for(int i=0;i<groups.length;i++){int x=(width-cols*(w+6))/2+(i%cols)*(w+6);int y=40+(i/cols)*150;
 for(int j=1;j<groups[i].length;j++){String n=groups[i][j];addRenderableWidget(Button.builder(label(n),b->{modules.put(n,!modules.get(n));b.setMessage(label(n));save();}).bounds(x+3,y+20+(j-1)*20,w-6,19).build());}}
 }
 Component label(String n){return Component.literal((modules.get(n)?"● ":"○ ")+n).withColor(modules.get(n)?0xC4A5FF:0xB0B0BE);}
 public void render(GuiGraphics g,int mx,int my,float delta){
 g.fill(0,0,width,height,0xE6101018);
 g.fill(width/2-100,5,width/2+100,31,0xFF29203F);
 g.fill(width/2-100,30,width/2+100,32,0xFF9966FF);
 g.drawCenteredString(font,"§lE N T E L  C L I E N T",width/2,12,0xFFC4A5FF);
 int cols=width>=620?5:width>=360?3:2;int w=Math.min(118,(width-20)/cols-6);
 for(int i=0;i<groups.length;i++){int x=(width-cols*(w+6))/2+(i%cols)*(w+6);int y=40+(i/cols)*150;g.fill(x,y,x+w,y+20+20*(groups[i].length-1)+4,0xFF20202D);g.fill(x,y,x+w,y+17,0xFF6845A5);g.drawCenteredString(font,groups[i][0],x+w/2,y+5,0xFFFFFFFF);}
 super.render(g,mx,my,delta);
 }
 public boolean isPauseScreen(){return false;}
 }
}
