# **EntityRenderer Source Code Analyze**

#### *由于个人码风习惯, Anal里的代码格式, 变量命名和源代码将会不同.*
#### *本文件的注释不会全部注释, 尤其是针对一些基础易懂的方法.*

```java
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)//"仅客户端"的注解, 渲染器有这个注解很正常
public abstract class EntityRenderer<T extends Entity>
{
    //常量
    protected static final float NAMETAG_SCALE = 0.025F;//姓名牌缩放比例, 是玩家实体使用的元素, 同样是有自定义名字的实体的元素
    public static final int LEASH_RENDER_STEPS = 24;//拴绳渲染的分段数

    //成员变量
    protected final EntityRenderDispatcher entityRenderDispatcher;//实体调度器
    private final Font font;//使用的字体, 推测和nametag协同
    protected float shadowRadius;//实体阴影半径
    protected float shadowStrength = 1.0F;//实体阴影深度
    
    /**
     * 由Eventbus读取并注册需要的构造函数, 由渲染注册事件调用，传入渲染系统提供的工具上下文
     * @param context 包含Dispatcher、Font等渲染所需的依赖对象
     */
    protected EntityRenderer(EntityRendererProvider.Context context)
    {
        this.entityRenderDispatcher = context.getEntityRenderDispatcher();
        this.font = context.getFont();
    }
    
    /**
     * 根据代码推测是返回实体当前tick的实体位置亮度和当前时间的世界亮度的综合结果的方法
     * @param entity 检测的实体, 一般是对应的目标实体
     * @param partialTicks 当前时间的局部刻, 我的世界是以1秒20tick的频率运行的, 局部刻是实际游戏帧数和tick的换算使用的, 以保证渲染正常
     * @return 综合后实体本身应该显示的亮度
     */
    public final int getPackedLightCoords(T entity, float partialTicks)
    {
        BlockPos blockpos = BlockPos.containing(entity.getLightProbePosition(partialTicks));
        return LightTexture.pack(this.getBlockLightLevel(entity, blockpos), this.getSkyLightLevel(entity, blockpos));
    }
    
    /**
     * 获取当前世界亮度的方法
     * @param entity 检测的实体.
     * @param pos 检测的位置
     * @return 世界亮度
     */
    protected int getSkyLightLevel(T entity, BlockPos pos) { return entity.level().getBrightness(LightLayer.SKY, pos); }
    
    /**
     * 获取当前方块亮度的方法
     */
    protected int getBlockLightLevel(T entity, BlockPos pos) { return entity.isOnFire() ? 15 : entity.level().getBrightness(LightLayer.BLOCK, pos); }
    
    /**
     * 检测实体是否应当被渲染的方法
     */
    public boolean shouldRender(T livingEntity, Frustum camera, double camX, double camY, double camZ)
    {
        if(!livingEntity.shouldRender(camX, camY, camZ))//包装到livingEntity的方法, 推测是不在视野内返回true, 然后反转为false
            return false;
        else if (livingEntity.noCulling)//如果实体没有剔除, 则返回true
            return true;
        else
        {
            AABB entityHitBox = livingEntity.getBoundingBoxForCulling().inflate(0.5);//检测实体的aabb(碰撞箱边界, 很抽象的缩写)
            if(entityHitBox.hasNaN() || entityHitBox.getSize() == 0.0)
                entityHitBox = new AABB(
                    livingEntity.getX() - 2.0,
                    livingEntity.getY() - 2.0,
                    livingEntity.getZ() - 2.0,
                    livingEntity.getX() + 2.0,
                    livingEntity.getY() + 2.0,
                    livingEntity.getZ() + 2.0
                );
            
            if(camera.isVisible(entityHitBox))//用Frustum类的isVisible方法检测是否可见
                return true;
            else
            {
                if(livingEntity instanceof Leashable leashable)//检测实体是否被拴绳拴住, 并进行对应的逻辑处理
                {
                    Entity leashHolder = leashable.getLeashHolder();
                    if(leashHolder != null)
                        return camera.isVisible(leashHolder.getBoundingBoxForCulling());
                }
                
                return false;
            }
        }
    }
    
    /**
     * 顾名思义, 该方法用于得到实际渲染的三轴偏移量
     * @return EntityRenderer由于是底层渲染器, 因此这是个钩子方法, 返回无偏移量
     */
    public Vec3 getRenderOffset(T entity, float partialTicks) { return Vec3.ZERO; }
    
    /**
     * 渲染实体的方法, 由于EntityRenderer为底层渲染器, 因此只有通用的必要渲染和事件推送, 没有实体本身的渲染逻辑
     * @param p_entity 渲染的实体, 命名中的p_式原版的混淆代码的标识符
     * @param entityYaw 实体偏航角, 实体经过插值平滑处理后的 Y 轴旋转角度<br><i>通俗点说, 就是实体身体朝向哪里</i>
     * @param partialTick 局部刻
     * @param poseStack 矩阵栈, 渲染方面的知识概念
     * @param bufferSource 根据变量名类型名"MultiBufferSource"可合理推测: 该参数涉及多重缓冲源, 当前用途不明
     * @param packedLight 综合后实体本身应该显示的亮度
     */
    public void render(T p_entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight)
    {
        if(p_entity instanceof Leashable leashable)//通过反射检测实体是否能被拴绳拴住
        {
            Entity leashHolder = leashable.getLeashHolder();//获取拴绳的主人(owner, 我英语不好只能先这样翻译了)
            if(leashHolder != null)
                this.renderLeash(p_entity, partialTick, poseStack, bufferSource, leashHolder);//渲染拴绳
        }
        
        // Neo: Post the RenderNameTagEvent and conditionally wrap #renderNameTag based on the result.
        //Dev的注释翻译: 把事件 "RenderNameTagEvent" 推送到客户端事件上, 根据结果的具体的情况打包#renderNameTag(很可能是我的世界原版的命名式二进制标签NBT)
        //妈的, 你们他妈的就不能维护下文档? NeoForge都有至少一年半了吧, 文档到现在都还不全, Stack Overflow也没有多少, 勾史😅😅
        //注: 在实际开发最好不要在局部变量外采用var.
        var nameTagEvent = new net.neoforged.neoforge.client.event.RenderNameTagEvent(
            p_entity, p_entity.getDisplayName(), this, 
            poseStack, bufferSource, packedLight, partialTick
        );

        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(nameTagEvent);
        if(nameTagEvent.canRender().isTrue() || nameTagEvent.canRender().isDefault() && this.shouldShowName(p_entity))
            this.renderNameTag(p_entity, nameTagEvent.getContent(), poseStack, bufferSource, packedLight, partialTick);
    }
    
    /**
     * 对, 这是渲染拴绳的方法.
     * @param entity 目标实体
     * @param partialTick 局部刻
     * @param poseStack 同上, 不详细展开了
     * @param bufferSource 同上, 不详细展开了
     * @param leashHolder 拴绳的主人, 由于主人可以不是实体(如栅栏), 因此得使用泛型元素
     * @param <E> 这不用多说了吧, 看不出来只能说Java得复习下泛型了
     */
    private <E extends Entity> void renderLeash(T entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, E leashHolder)
    {
        poseStack.pushPose();//个人推测: pushPose和popPose是类似于H5的<>的</>存在, 内部包裹的就是实体渲染的状态, 方法名也能对的上
        
        //CLARIFY: 下面的都是计算渲染位置的流程, 由于拴绳逻辑是通用的, 模组一般来说是无需修改的, 所以就不看了
        Vec3 vec3 = leashHolder.getRopeHoldPosition(partialTick);//得到主人的位置坐标系三个数值
        double d0 = (double)(entity.getPreciseBodyRotation(partialTick) * (float) (Math.PI / 180.0)) + (Math.PI / 2);//得到当前局部刻实体的身体精准旋转量
        Vec3 vec31 = entity.getLeashOffset(partialTick);//得到实体的拴绳偏移量
        //🤓🤓 Nerd Stuff 总结( 瞎 猜 ): 计算两个实体的距离, 得出拴绳渲染的抛物线实现渲染, 拴绳当然不能是直线, 你懂的
        double d1 = Math.cos(d0) * vec31.z + Math.sin(d0) * vec31.x;
        double d2 = Math.sin(d0) * vec31.z - Math.cos(d0) * vec31.x;
        double d3 = Mth.lerp((double)partialTick, entity.xo, entity.getX()) + d1;
        double d4 = Mth.lerp((double)partialTick, entity.yo, entity.getY()) + vec31.y;
        double d5 = Mth.lerp((double)partialTick, entity.zo, entity.getZ()) + d2;
        poseStack.translate(d1, vec31.y, d2);
        float f = (float)(vec3.x - d3);
        float f1 = (float)(vec3.y - d4);
        float f2 = (float)(vec3.z - d5);
        float f3 = 0.025F;
        VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.leash());
        Matrix4f matrix4f = poseStack.last().pose();
        float f4 = Mth.invSqrt(f * f + f2 * f2) * 0.025F / 2.0F;
        float f5 = f2 * f4;
        float f6 = f * f4;
        BlockPos blockpos = BlockPos.containing(entity.getEyePosition(partialTick));
        BlockPos blockpos1 = BlockPos.containing(leashHolder.getEyePosition(partialTick));
        int i = this.getBlockLightLevel(entity, blockpos);
        int j = this.entityRenderDispatcher.getRenderer(leashHolder).getBlockLightLevel(leashHolder, blockpos1);
        int k = entity.level().getBrightness(LightLayer.SKY, blockpos);
        int l = entity.level().getBrightness(LightLayer.SKY, blockpos1);
        
        for (int i1 = 0; i1 <= 24; i1++)
            addVertexPair(vertexconsumer, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.025F, f5, f6, i1, false);
    
        
        for (int j1 = 24; j1 >= 0; j1--)
            addVertexPair(vertexconsumer, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.0F, f5, f6, j1, true);
        
        poseStack.popPose();
        //嗯嗯, 所以还是看了🤔🤔
    }
    
    /**
     * 这个真不懂, 只知道和顶点有关
     * 查阅补充, 这个方法是在空间中画两个交叉的矩形面, 以此来模拟一根有厚度的绳子
     */
    private static void addVertexPair(
        VertexConsumer buffer,
        Matrix4f pose,
        float startX,
        float startY,
        float startZ,
        int entityBlockLight,
        int holderBlockLight,
        int entitySkyLight,
        int holderSkyLight,
        float yOffset,
        float dy,
        float dx,
        float dz,
        int index,
        boolean reverse
    ) {
        float f = (float)index / 24.0F;
        int i = (int)Mth.lerp(f, (float)entityBlockLight, (float)holderBlockLight);
        int j = (int)Mth.lerp(f, (float)entitySkyLight, (float)holderSkyLight);
        int k = LightTexture.pack(i, j);
        float f1 = index % 2 == (reverse ? 1 : 0) ? 0.7F : 1.0F;
        float f2 = 0.5F * f1;
        float f3 = 0.4F * f1;
        float f4 = 0.3F * f1;
        float f5 = startX * f;
        float f6 = startY > 0.0F ? startY * f * f : startY - startY * (1.0F - f) * (1.0F - f);
        float f7 = startZ * f;
        buffer.addVertex(pose, f5 - dx, f6 + dy, f7 + dz).setColor(f2, f3, f4, 1.0F).setLight(k);
        buffer.addVertex(pose, f5 + dx, f6 + yOffset - dy, f7 - dz).setColor(f2, f3, f4, 1.0F).setLight(k);
    }
    
    /**
     * 检测实体是否应该展示名称的方法.
     * @param entity 被检测的实体.
     * @return 看不懂返回值是啥就别学了
     */
    protected boolean shouldShowName(T entity)
    {
        //条件: 1. 实体的属性被标记为 "应该显示名称", 或者实体有自定义名称 2. 当前实体被玩家准心指向
        return entity.shouldShowName() || entity.hasCustomName() && entity == this.entityRenderDispatcher.crosshairPickEntity;
    }
    
    /**
     * Returns the location of an entity's texture.
     * EZ, 不写了
     */
    public abstract ResourceLocation getTextureLocation(T entity);
    
    /**
     * 字体的getter方法
     */
    public Font getFont() { return this.font; }
    
    /**
     * 在实体头顶上渲染其名字的方法.
     * @param entity 对应的实体.
     * @param displayName 展示的名字.
     * @param poseStack 矩阵栈
     * @param bufferSource 同上
     * @param packedLight 同上
     * @param partialTick 同上
     */
    protected void renderNameTag(T entity, Component displayName, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTick) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(entity);//获取开根后的实体与玩家的距离值
        if(net.neoforged.neoforge.client.ClientHooks.isNameplateInRenderDistance(entity, d0))//通过客户端事件钩获取是否有NameTag在渲染距离内
        {
            Vec3 vec3 = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, entity.getViewYRot(partialTick));//获取实体的附加件(大概), 可为空值
            if(vec3 != null)//检测值是否为空
            {
                boolean flag = !entity.isDiscrete();//检测实体是否为离散的(?) CLARIFY: md, 看了下源代码, 什么可爱离散, 就是检测玩家潜行了没有, ojng你可真可爱啊
                int i = "deadmau5".equals(displayName.getString()) ? -10 : 0;
                //CLARIFY: 查了一下, 彩蛋设定, 如果玩家被命名为"deadmau5", 它的NameTag会被抬高10px
                poseStack.pushPose();
                poseStack.translate(vec3.x, vec3.y + 0.5, vec3.z);//使坐标向上偏移0.5
                poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());//使渲染对象(NameTag)始终面向玩家
                poseStack.scale(0.025F, -0.025F, 0.025F);//缩放渲染对象, 比例0.025x
                Matrix4f matrix4f = poseStack.last().pose();
                float f = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);//设置渲染透明度
                int j = (int)(f * 255.0F) << 24;//位运算, 看不懂捏
                Font font = this.getFont();
                float f1 = (float)(-font.width(displayName) / 2);
                //如果有flag, 那么字体的渲染最终会是SEE_THROUGH(透过)的渲染方式, 否则则是正常渲染, if语句的内容是辅助性渲染
                font.drawInBatch(
                    displayName, f1, (float)i, 553648127, false, matrix4f, bufferSource, flag ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, j, packedLight
                );
                if(flag)
                    font.drawInBatch(displayName, f1, (float)i, -1, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
                
                poseStack.popPose();
            }
        }
    }
    
    /**
     * 阴影半径的getter方法
     */
    protected float getShadowRadius(T entity) { return this.shadowRadius; }
}

```