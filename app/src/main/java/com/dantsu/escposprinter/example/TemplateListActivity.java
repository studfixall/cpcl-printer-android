package com.dantsu.escposprinter.example;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.dantsu.escposprinter.template.LabelTemplate;
import com.dantsu.escposprinter.template.TemplateManager;

import java.util.List;

/**
 * 模板列表 Activity
 * 显示和管理所有标签模板
 */
public class TemplateListActivity extends AppCompatActivity {

    private TemplateManager templateManager;
    private ListView listView;
    private TemplateAdapter adapter;
    private List<LabelTemplate> templates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_list);

        templateManager = new TemplateManager(this);
        listView = findViewById(R.id.listView);

        findViewById(R.id.fabAdd).setOnClickListener(v -> createNewTemplate());

        listView.setOnItemClickListener((parent, view, position, id) -> {
            LabelTemplate template = templates.get(position);
            editTemplate(template);
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            LabelTemplate template = templates.get(position);
            showTemplateOptions(template);
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTemplates();
    }

    private void loadTemplates() {
        templates = templateManager.getAllTemplates();

        // 如果没有模板，创建默认模板
        if (templates.isEmpty()) {
            templateManager.createDefaultTemplate();
            templates = templateManager.getAllTemplates();
        }

        adapter = new TemplateAdapter();
        listView.setAdapter(adapter);
    }

    private void createNewTemplate() {
        Intent intent = new Intent(this, TemplateEditActivity.class);
        startActivity(intent);
    }

    private void editTemplate(LabelTemplate template) {
        Intent intent = new Intent(this, TemplateEditActivity.class);
        intent.putExtra("template_id", template.getId());
        startActivity(intent);
    }

    private void showTemplateOptions(LabelTemplate template) {
        String[] options = {"编辑", "复制", "设为默认", "删除"};

        new AlertDialog.Builder(this)
            .setTitle(template.getName())
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        editTemplate(template);
                        break;
                    case 1:
                        copyTemplate(template);
                        break;
                    case 2:
                        setDefaultTemplate(template);
                        break;
                    case 3:
                        deleteTemplate(template);
                        break;
                }
            })
            .show();
    }

    private void copyTemplate(LabelTemplate template) {
        LabelTemplate copy = template.copy();
        templateManager.saveTemplate(copy);
        loadTemplates();
        Toast.makeText(this, "模板已复制", Toast.LENGTH_SHORT).show();
    }

    private void setDefaultTemplate(LabelTemplate template) {
        templateManager.setDefaultTemplate(template.getId());
        loadTemplates();
        Toast.makeText(this, "已设为默认模板", Toast.LENGTH_SHORT).show();
    }

    private void deleteTemplate(LabelTemplate template) {
        new AlertDialog.Builder(this)
            .setTitle("确认删除")
            .setMessage("确定要删除模板 \"" + template.getName() + "\" 吗？")
            .setPositiveButton("删除", (dialog, which) -> {
                templateManager.deleteTemplate(template.getId());
                loadTemplates();
                Toast.makeText(this, "模板已删除", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("取消", null)
            .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_template_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            loadTemplates();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class TemplateAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return templates.size();
        }

        @Override
        public Object getItem(int position) {
            return templates.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_template, parent, false);
                holder = new ViewHolder();
                holder.tvName = convertView.findViewById(R.id.tvName);
                holder.tvSize = convertView.findViewById(R.id.tvSize);
                holder.tvDefault = convertView.findViewById(R.id.tvDefault);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            LabelTemplate template = templates.get(position);
            holder.tvName.setText(template.getName());
            holder.tvSize.setText(template.getWidth() + "x" + template.getHeight() + "mm @ " + template.getDpi() + "DPI");
            holder.tvDefault.setVisibility(template.isDefault() ? View.VISIBLE : View.GONE);

            return convertView;
        }
    }

    private static class ViewHolder {
        TextView tvName;
        TextView tvSize;
        TextView tvDefault;
    }
}
