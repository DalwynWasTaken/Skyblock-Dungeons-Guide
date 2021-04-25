package kr.syeyoung.dungeonsguide.features.impl.party.playerpreview;

import kr.syeyoung.dungeonsguide.config.guiconfig.GuiConfig;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataRendererEditor extends MPanel {
    private FeatureViewPlayerOnJoin feature;
    private GuiConfig config;

    public DataRendererEditor(GuiConfig config, FeatureViewPlayerOnJoin featureViewPlayerOnJoin) {
        this.config = config;
        this.feature = featureViewPlayerOnJoin;
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(5,5,parentWidth-10, 260));
    }

    private String currentlySelected;
    private int selectedX;
    private int selectedY;
    private int lastX;
    private int lastY;
    private int scrollY;
    private final int baseWidth = 100;
    private final int hamburgerWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth("==");

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        Gui.drawRect(0,0,getBounds().width, getBounds().height, 0xFF444444);

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());


        fr.drawString("Current", (baseWidth + hamburgerWidth+10 -fr.getStringWidth("Current")) /2 , 4, 0xFFFFFFFF);
        Gui.drawRect(4,4 + fr.FONT_HEIGHT + 3,baseWidth + hamburgerWidth+6 + 1, 236+ fr.FONT_HEIGHT + 3, 0xFF222222);
        Gui.drawRect(5,5+ fr.FONT_HEIGHT + 3,baseWidth + hamburgerWidth + 5 + 1, 235+ fr.FONT_HEIGHT + 3, 0xFF555555);
        Gui.drawRect(5 + hamburgerWidth,4+ fr.FONT_HEIGHT + 3,6 + hamburgerWidth, 236+ fr.FONT_HEIGHT + 3, 0xFF222222);

        fr.drawString("Available", (310 + baseWidth + hamburgerWidth -fr.getStringWidth("Available")) / 2, 4, 0xFFFFFFFF);
        Gui.drawRect(154,4 + fr.FONT_HEIGHT + 3,150 + baseWidth + hamburgerWidth + 6+1, 236+ fr.FONT_HEIGHT + 3, 0xFF222222);
        Gui.drawRect(155,5+ fr.FONT_HEIGHT + 3,150 + baseWidth + hamburgerWidth + 5+1, 235+ fr.FONT_HEIGHT + 3, 0xFF555555);
        Gui.drawRect(155 + hamburgerWidth,4 + fr.FONT_HEIGHT + 3,156 + hamburgerWidth, 236+ fr.FONT_HEIGHT + 3, 0xFF222222);

        GlStateManager.pushMatrix();
        clip(sr, scissor.x + 6+hamburgerWidth, scissor.y + 5+fr.FONT_HEIGHT+3, baseWidth, 230);
        GlStateManager.translate(6+hamburgerWidth, 5+fr.FONT_HEIGHT+3, 0);
        int culmutativeY = 0;
        int relSelectedY = selectedY - (5+ fr.FONT_HEIGHT + 3);
        boolean drewit = false;
        for (String datarenderers : feature.<List<String>>getParameter("datarenderers").getValue()) {

            if (0 <= selectedX && selectedX <= hamburgerWidth+11 && currentlySelected != null) {
                DataRenderer dataRenderer = DataRendererRegistry.getDataRenderer(currentlySelected);
                Dimension dim;
                if (dataRenderer == null) dim = new Dimension(0,fr.FONT_HEIGHT*2);
                else dim = dataRenderer.getDimension();

                if (culmutativeY + dim.height > relSelectedY && relSelectedY >= culmutativeY && !drewit) {
                    clip(sr, scissor.x + 6 + hamburgerWidth, scissor.y + 5+fr.FONT_HEIGHT+3, baseWidth, 230);
                    if (dataRenderer == null) {
                        fr.drawString("Couldn't find Datarenderer", 0,0, 0xFFFF0000);
                        fr.drawString(currentlySelected, 0,fr.FONT_HEIGHT, 0xFFFF0000);
                        dim = new Dimension(0, fr.FONT_HEIGHT * 2);
                    } else {
                        GlStateManager.pushMatrix();
                        dim = dataRenderer.renderDummy();
                        GlStateManager.popMatrix();
                    }
                    clip(sr, scissor.x, scissor.y, scissor.width, scissor.height);
                    GlStateManager.translate(-hamburgerWidth-1, 0, 0);
                    Gui.drawRect(0,0, hamburgerWidth, dim.height-1, 0xFF777777);
                    fr.drawString("=",fr.getStringWidth("=")/2,(dim.height - fr.FONT_HEIGHT) / 2, 0xFFFFFFFF);
                    GlStateManager.translate(hamburgerWidth+1,dim.height,0);
                    drewit = true;
                }
            }

            DataRenderer dataRenderer = DataRendererRegistry.getDataRenderer(datarenderers);
            clip(sr, scissor.x + 6 + hamburgerWidth, scissor.y + 5+fr.FONT_HEIGHT+3, baseWidth, 230);
            Dimension dim;

            if (dataRenderer == null) {
                fr.drawString("Couldn't find Datarenderer", 0,0, 0xFFFF0000);
                fr.drawString(datarenderers, 0,fr.FONT_HEIGHT, 0xFFFF0000);
                dim = new Dimension(0, fr.FONT_HEIGHT * 2);
            } else {
                GlStateManager.pushMatrix();
                dim = dataRenderer.renderDummy();
                GlStateManager.popMatrix();
            }
            clip(sr, scissor.x, scissor.y, scissor.width, scissor.height);
            GlStateManager.translate(-hamburgerWidth-1, 0, 0);
            Gui.drawRect(0,0, hamburgerWidth, dim.height-1, 0xFFAAAAAA);
            fr.drawString("=",fr.getStringWidth("=")/2,(dim.height - fr.FONT_HEIGHT) / 2, 0xFFFFFFFF);
            GlStateManager.translate(hamburgerWidth+1,dim.height,0);

            culmutativeY += dim.height;
        }

        if (currentlySelected != null && new Rectangle(0,5+fr.FONT_HEIGHT + 3, hamburgerWidth+11, 232).contains(selectedX, selectedY) && !drewit) {
            DataRenderer dataRenderer = DataRendererRegistry.getDataRenderer(currentlySelected);
            Dimension dim;
                clip(sr, scissor.x + 6 + hamburgerWidth, scissor.y + 5+fr.FONT_HEIGHT+3, baseWidth, 230);
                if (dataRenderer == null) {
                    fr.drawString("Couldn't find Datarenderer", 0,0, 0xFFFF0000);
                    fr.drawString(currentlySelected, 0,fr.FONT_HEIGHT, 0xFFFF0000);
                    dim = new Dimension(0, fr.FONT_HEIGHT * 2);
                } else {
                    GlStateManager.pushMatrix();
                    dim = dataRenderer.renderDummy();
                    GlStateManager.popMatrix();
                }
                clip(sr, scissor.x, scissor.y, scissor.width, scissor.height);
                GlStateManager.translate(-hamburgerWidth-1, 0, 0);
                Gui.drawRect(0,0, hamburgerWidth, dim.height-1, 0xFF777777);
                fr.drawString("=",fr.getStringWidth("=")/2,(dim.height - fr.FONT_HEIGHT) / 2, 0xFFFFFFFF);
                GlStateManager.translate(hamburgerWidth+1,dim.height,0);
        }
        GlStateManager.popMatrix();


        GlStateManager.pushMatrix();
        GlStateManager.translate(156+hamburgerWidth, 5+fr.FONT_HEIGHT+3 - scrollY, 0);

        Set<String> rest = new HashSet<>(DataRendererRegistry.getValidDataRenderer());
        rest.removeAll( feature.<List<String>>getParameter("datarenderers").getValue());
        rest.remove(currentlySelected);
        for (String datarenderers : rest) {
            DataRenderer dataRenderer = DataRendererRegistry.getDataRenderer(datarenderers);
            clip(sr, scissor.x + 156 + hamburgerWidth, scissor.y + 5+fr.FONT_HEIGHT+3, baseWidth, 230);
            Dimension dim;
            if (dataRenderer == null) {
                fr.drawString("Couldn't find Datarenderer", 0,0, 0xFFFF0000);
                fr.drawString(datarenderers, 0,fr.FONT_HEIGHT, 0xFFFF0000);
                dim = new Dimension(0, fr.FONT_HEIGHT * 2);
            } else {
                GlStateManager.pushMatrix();
                dim = dataRenderer.renderDummy();
                GlStateManager.popMatrix();
            }
            clip(sr, scissor.x + 156, scissor.y + 5+fr.FONT_HEIGHT+3, hamburgerWidth, 230);
            GlStateManager.translate(-hamburgerWidth-1, 0, 0);
            Gui.drawRect(0,0, hamburgerWidth, dim.height-1, 0xFFAAAAAA);
            fr.drawString("=",fr.getStringWidth("=")/2,(dim.height - fr.FONT_HEIGHT) / 2, 0xFFFFFFFF);
            GlStateManager.translate(hamburgerWidth+1,dim.height,0);
        }
        GlStateManager.popMatrix();
        clip(sr, 0,0,sr.getScaledWidth(), sr.getScaledHeight());
        {
            if (currentlySelected != null) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(selectedX+hamburgerWidth+1, selectedY, 0);
                DataRenderer dataRenderer = DataRendererRegistry.getDataRenderer(currentlySelected);
                Dimension dim;
                if (dataRenderer == null) {
                    fr.drawString("Couldn't find Datarenderer", 0, 0, 0xFFFF0000);
                    fr.drawString(currentlySelected, 0, fr.FONT_HEIGHT, 0xFFFF0000);
                    dim = new Dimension(0, fr.FONT_HEIGHT * 2);
                } else {
                    GlStateManager.pushMatrix();
                    dim = dataRenderer.renderDummy();
                    GlStateManager.popMatrix();
                }
                GlStateManager.translate(-hamburgerWidth-1, 0, 0);
                Gui.drawRect(0,0, hamburgerWidth, dim.height-1, 0xFFAAAAAA);
                fr.drawString("=",fr.getStringWidth("=")/2,(dim.height - fr.FONT_HEIGHT) / 2, 0xFFFFFFFF);
                GlStateManager.popMatrix();
            }
        }
        clip(sr, scissor.x, scissor.y, scissor.width, scissor.height);
    }

    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        super.mouseClicked(absMouseX, absMouseY, relMouseX, relMouseY, mouseButton);
        lastX = relMouseX;
        lastY = relMouseY;
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        int legitRelY = relMouseY - (5+fr.FONT_HEIGHT+3);
        if (new Rectangle(155,5+fr.FONT_HEIGHT + 3, hamburgerWidth, 230).contains(relMouseX, relMouseY)) {
            Set<String> rest = new HashSet<>(DataRendererRegistry.getValidDataRenderer());
            rest.removeAll( feature.<List<String>>getParameter("datarenderers").getValue());
            rest.remove(currentlySelected);
            int culmutativeY  = 0;
            for (String datarenderers : rest) {
                DataRenderer dataRenderer = DataRendererRegistry.getDataRenderer(datarenderers);
                Dimension dim;
                if (dataRenderer == null) {
                    dim = new Dimension(0, fr.FONT_HEIGHT * 2);
                } else {
                    GlStateManager.pushMatrix();
                    dim = dataRenderer.getDimension();
                    GlStateManager.popMatrix();
                }
                culmutativeY += dim.height;

                if (legitRelY < culmutativeY) {
                    currentlySelected = datarenderers;
                    selectedX = 155;
                    selectedY = culmutativeY - dim.height + 5+fr.FONT_HEIGHT + 3 - scrollY;
                    break;
                }
            }
        }
        if (new Rectangle(5,5+fr.FONT_HEIGHT + 3, hamburgerWidth, 230).contains(relMouseX, relMouseY)) {
            List<String> rest = feature.<List<String>>getParameter("datarenderers").getValue();
            int culmutativeY  = 0;
            for (String datarenderers : rest) {
                DataRenderer dataRenderer = DataRendererRegistry.getDataRenderer(datarenderers);
                Dimension dim;
                if (dataRenderer == null) {
                    dim = new Dimension(0, fr.FONT_HEIGHT * 2);
                } else {
                    GlStateManager.pushMatrix();
                    dim = dataRenderer.getDimension();
                    GlStateManager.popMatrix();
                }
                culmutativeY += dim.height;

                if (legitRelY < culmutativeY) {
                    currentlySelected = datarenderers;
                    selectedX = 5;
                    selectedY = culmutativeY - dim.height + 5+fr.FONT_HEIGHT + 3;
                    rest.remove(datarenderers);
                    break;
                }
            }
        }
    }

    @Override
    public void mouseClickMove(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(absMouseX, absMouseY, relMouseX, relMouseY, clickedMouseButton, timeSinceLastClick);
        if (currentlySelected != null) {
            int dx = relMouseX - lastX;
            int dy = relMouseY - lastY;
            selectedX += dx;
            selectedY += dy;
        }
        lastX = relMouseX;
        lastY = relMouseY;
    }

    @Override
    public void mouseReleased(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int state) {
        super.mouseReleased(absMouseX, absMouseY, relMouseX, relMouseY, state);
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        int legitRelY = selectedY - (5+fr.FONT_HEIGHT+3);
        if (currentlySelected != null && new Rectangle(0,5+fr.FONT_HEIGHT + 3, hamburgerWidth+11, 232).contains(selectedX, selectedY)) {
            Set<String> rest = new HashSet<>(DataRendererRegistry.getValidDataRenderer());
            int culmutativeY  = 0;
            List<String > asdasdkasd = feature.<List<String>>getParameter("datarenderers").getValue();
            int index = asdasdkasd.size();
            for (int i = 0; i <asdasdkasd.size(); i++) {
                String datarenderers = asdasdkasd.get(i);
                DataRenderer dataRenderer = DataRendererRegistry.getDataRenderer(datarenderers);
                Dimension dim;
                if (dataRenderer == null) {
                    dim = new Dimension(0, fr.FONT_HEIGHT * 2);
                } else {
                    GlStateManager.pushMatrix();
                    dim = dataRenderer.getDimension();
                    GlStateManager.popMatrix();
                }
                culmutativeY += dim.height;

                if (legitRelY < culmutativeY) {
                    index = i;
                    break;
                }
            }

            asdasdkasd.add(index, currentlySelected);
        }

        currentlySelected = null;
    }

    @Override
    public void mouseScrolled(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {
        super.mouseScrolled(absMouseX, absMouseY, relMouseX0, relMouseY0, scrollAmount);

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        if (!new Rectangle(155,5+fr.FONT_HEIGHT + 3, hamburgerWidth, 230).contains(relMouseX0, relMouseY0)) return;

        if (scrollAmount > 0) scrollY += 20;
        if (scrollAmount < 0) scrollY -= 20;
        if (scrollY < 0) scrollY = 0;
    }
}
