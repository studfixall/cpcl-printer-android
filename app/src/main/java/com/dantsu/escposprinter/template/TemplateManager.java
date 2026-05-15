package com.dantsu.escposprinter.template;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 模板管理器
 * 负责模板的保存、加载、删除等操作
 */
public class TemplateManager {
    
    private static final String PREFS_NAME = "LabelTemplates";
    private static final String KEY_TEMPLATES = "templates";
    private static final String TEMPLATES_DIR = "templates";
    
    private Context context;
    private SharedPreferences prefs;
    private Gson gson;
    
    public TemplateManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        ensureTemplatesDir();
    }
    
    /**
     * 确保模板目录存在
     */
    private void ensureTemplatesDir() {
        File dir = new File(context.getFilesDir(), TEMPLATES_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    /**
     * 获取模板文件
     */
    private File getTemplateFile(String templateId) {
        return new File(new File(context.getFilesDir(), TEMPLATES_DIR), templateId + ".json");
    }
    
    /**
     * 保存模板
     */
    public boolean saveTemplate(LabelTemplate template) {
        try {
            File file = getTemplateFile(template.getId());
            FileWriter writer = new FileWriter(file);
            gson.toJson(template, writer);
            writer.close();
            
            // 更新模板列表
            addToTemplateList(template.getId(), template.getName());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 加载模板
     */
    public LabelTemplate loadTemplate(String templateId) {
        try {
            File file = getTemplateFile(templateId);
            if (!file.exists()) {
                return null;
            }
            FileReader reader = new FileReader(file);
            LabelTemplate template = gson.fromJson(reader, LabelTemplate.class);
            reader.close();
            return template;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 删除模板
     */
    public boolean deleteTemplate(String templateId) {
        File file = getTemplateFile(templateId);
        boolean deleted = file.delete();
        if (deleted) {
            removeFromTemplateList(templateId);
        }
        return deleted;
    }
    
    /**
     * 获取所有模板
     */
    public List<LabelTemplate> getAllTemplates() {
        List<LabelTemplate> templates = new ArrayList<>();
        List<TemplateInfo> infos = getTemplateList();
        
        for (TemplateInfo info : infos) {
            LabelTemplate template = loadTemplate(info.id);
            if (template != null) {
                templates.add(template);
            }
        }
        
        return templates;
    }
    
    /**
     * 获取默认模板
     */
    public LabelTemplate getDefaultTemplate() {
        List<LabelTemplate> templates = getAllTemplates();
        for (LabelTemplate template : templates) {
            if (template.isDefault()) {
                return template;
            }
        }
        // 如果没有默认模板，返回第一个
        return templates.isEmpty() ? null : templates.get(0);
    }
    
    /**
     * 设置默认模板
     */
    public void setDefaultTemplate(String templateId) {
        // 清除所有默认标记
        List<LabelTemplate> templates = getAllTemplates();
        for (LabelTemplate template : templates) {
            if (template.isDefault()) {
                template.setDefault(false);
                saveTemplate(template);
            }
        }
        
        // 设置新的默认模板
        LabelTemplate newDefault = loadTemplate(templateId);
        if (newDefault != null) {
            newDefault.setDefault(true);
            saveTemplate(newDefault);
        }
    }
    
    /**
     * 创建默认模板
     */
    public LabelTemplate createDefaultTemplate() {
        LabelTemplate template = new LabelTemplate("默认模板", 50, 30, 203);
        template.setDescription("50x30mm 基础标签模板");
        
        // 添加默认组件
        TextComponent title = new TextComponent("title");
        title.setX(50);
        title.setY(10);
        title.setStaticValue("商品名称");
        title.setFontWidth(2);
        title.setFontHeight(2);
        title.setBold(true);
        template.addComponent(title);
        
        BarcodeComponent barcode = new BarcodeComponent("barcode");
        barcode.setX(50);
        barcode.setY(50);
        barcode.setWidth(200);
        barcode.setHeight(60);
        barcode.setBarcodeType(BarcodeComponent.BarcodeType.CODE128);
        barcode.setStaticValue("1234567890");
        template.addComponent(barcode);
        
        TextComponent price = new TextComponent("price");
        price.setX(50);
        price.setY(130);
        price.setStaticValue("¥99.00");
        price.setFontWidth(2);
        price.setFontHeight(2);
        template.addComponent(price);
        
        saveTemplate(template);
        return template;
    }
    
    /**
     * 获取模板列表（用于快速浏览）
     */
    private List<TemplateInfo> getTemplateList() {
        String json = prefs.getString(KEY_TEMPLATES, "[]");
        Type type = new TypeToken<List<TemplateInfo>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    /**
     * 添加到模板列表
     */
    private void addToTemplateList(String id, String name) {
        List<TemplateInfo> list = getTemplateList();
        
        // 检查是否已存在
        boolean exists = false;
        for (TemplateInfo info : list) {
            if (info.id.equals(id)) {
                info.name = name;
                exists = true;
                break;
            }
        }
        
        if (!exists) {
            list.add(new TemplateInfo(id, name));
        }
        
        prefs.edit().putString(KEY_TEMPLATES, gson.toJson(list)).apply();
    }
    
    /**
     * 从模板列表移除
     */
    private void removeFromTemplateList(String id) {
        List<TemplateInfo> list = getTemplateList();
        list.removeIf(info -> info.id.equals(id));
        prefs.edit().putString(KEY_TEMPLATES, gson.toJson(list)).apply();
    }
    
    /**
     * 模板信息（轻量级）
     */
    private static class TemplateInfo {
        String id;
        String name;
        
        TemplateInfo(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
