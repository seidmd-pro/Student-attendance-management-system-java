package com.attendance.ui.admin;

import com.attendance.dao.ClassCourseDAO;
import com.attendance.dao.ClassRoomDAO;
import com.attendance.dao.CourseDAO;
import com.attendance.dao.TeacherDAO;
import com.attendance.model.ClassCourse;
import com.attendance.model.ClassRoom;
import com.attendance.model.Course;
import com.attendance.model.Teacher;
import com.attendance.ui.common.BaseCrudPanel;
import com.attendance.util.UIUtil;

import javax.swing.*;
import java.awt.*;

public class ClassSubjectPanel extends BaseCrudPanel {

    private final ClassCourseDAO dao = new ClassCourseDAO();

    public ClassSubjectPanel() {
        super("Assign Teachers to Classes", new String[]{"ID", "Class", "Course", "Teacher"});
    }

    @Override
    protected void loadData() {
        clearTable();
        try {
            for (ClassCourse cc : dao.findAll())
                tableModel.addRow(new Object[]{cc.getId(), cc.getClassName(), cc.getCourseName(), cc.getTeacherName()});
            afterLoad();
        } catch (Exception ex) { UIUtil.showError(this, ex.getMessage()); }
    }

    @Override protected void openAddDialog() { showDialog(null); }
    @Override protected void openEditDialog() {
        try { showDialog(dao.findById(getSelectedId())); }
        catch (Exception ex) { UIUtil.showError(this, ex.getMessage()); }
    }
    @Override protected void deleteSelected() {
        try { dao.delete(getSelectedId()); loadData(); }
        catch (Exception ex) { UIUtil.showError(this, "Delete failed: " + ex.getMessage()); }
    }

    private void showDialog(ClassCourse existing) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
            existing == null ? "Assign Teacher" : "Edit Assignment",
            java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(440, 280);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JComboBox<ClassRoom> classCombo  = new JComboBox<>();
        JComboBox<Course>    courseCombo = new JComboBox<>();
        JComboBox<Teacher>   teacherCombo= new JComboBox<>();
        classCombo.setFont(UIUtil.FONT_BODY);
        courseCombo.setFont(UIUtil.FONT_BODY);
        teacherCombo.setFont(UIUtil.FONT_BODY);

        try {
            for (ClassRoom cl : new ClassRoomDAO().findAll()) classCombo.addItem(cl);
            for (Course c    : new CourseDAO().findAll())     courseCombo.addItem(c);
            teacherCombo.addItem(null);
            for (Teacher t   : new TeacherDAO().findAll())    teacherCombo.addItem(t);
        } catch (Exception ignored) {}

        if (existing != null) {
            for (int i = 0; i < classCombo.getItemCount(); i++)
                if (classCombo.getItemAt(i).getId() == existing.getClassId()) { classCombo.setSelectedIndex(i); break; }
            for (int i = 0; i < courseCombo.getItemCount(); i++)
                if (courseCombo.getItemAt(i).getId() == existing.getCourseId()) { courseCombo.setSelectedIndex(i); break; }
            for (int i = 0; i < teacherCombo.getItemCount(); i++) {
                Teacher t = teacherCombo.getItemAt(i);
                if (t != null && t.getId() == existing.getTeacherId()) { teacherCombo.setSelectedIndex(i); break; }
            }
        }

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        p.setBackground(Color.WHITE);
        p.add(UIUtil.formRow("Class *",   classCombo));   p.add(Box.createVerticalStrut(10));
        p.add(UIUtil.formRow("Course *",  courseCombo));  p.add(Box.createVerticalStrut(10));
        p.add(UIUtil.formRow("Teacher",   teacherCombo)); p.add(Box.createVerticalStrut(16));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setOpaque(false);
        JButton save = UIUtil.primaryButton("Save");
        JButton cancel = UIUtil.secondaryButton("Cancel");
        btns.add(cancel); btns.add(save);
        p.add(btns);

        save.addActionListener(e -> {
            try {
                ClassRoom cl = (ClassRoom) classCombo.getSelectedItem();
                Course co    = (Course)    courseCombo.getSelectedItem();
                Teacher tch  = (Teacher)   teacherCombo.getSelectedItem();
                if (cl == null || co == null) throw new IllegalArgumentException("Class and Course are required.");
                ClassCourse cc = existing != null ? existing : new ClassCourse();
                cc.setClassId(cl.getId());
                cc.setCourseId(co.getId());
                cc.setTeacherId(tch != null ? tch.getId() : 0);
                if (existing == null) dao.insert(cc); else dao.update(cc);
                dlg.dispose(); loadData();
            } catch (Exception ex) { UIUtil.showError(dlg, ex.getMessage()); }
        });
        cancel.addActionListener(e -> dlg.dispose());
        dlg.setContentPane(p);
        dlg.setVisible(true);
    }
}
