<!-- 教师管理页面 - 若依风格 -->
<template>
  <div class="ruoyi-container">
    <!-- 查询表单 -->
    <div class="ruoyi-card search-card">
      <el-form :inline="true" :model="queryParams" class="search-form" ref="queryForm">
        <el-form-item label="教师姓名" prop="teacherName">
          <el-input
            v-model="queryParams.teacherName"
            placeholder="请输入教师姓名"
            clearable
            style="width: 200px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="所属学院" prop="institute">
          <el-input
            v-model="queryParams.institute"
            placeholder="请输入所属学院"
            clearable
            style="width: 200px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="职称" prop="type">
          <el-select v-model="queryParams.type" placeholder="请选择职称" clearable style="width: 200px">
            <el-option label="教授" value="教授" />
            <el-option label="副教授" value="副教授" />
            <el-option label="讲师" value="讲师" />
            <el-option label="助教" value="助教" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮 -->
    <div class="ruoyi-card toolbar-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button
            type="primary"
            plain
            icon="el-icon-plus"
            @click="handleAdd"
          >新增教师</el-button>
          <el-button
            type="danger"
            plain
            icon="el-icon-delete"
            :disabled="multiple"
            @click="handleDelete"
          >批量删除</el-button>
        </div>
        <div class="toolbar-right">
          <el-tooltip content="刷新" placement="top">
            <el-button size="mini" circle icon="el-icon-refresh" @click="getList"></el-button>
          </el-tooltip>
        </div>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="ruoyi-card table-card">
      <el-table 
        :data="pagination.records" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        class="ruoyi-table">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="teacherId" label="教师ID" width="100" align="center" />
        <el-table-column prop="teacherName" label="教师姓名" width="120" align="center" show-overflow-tooltip />
        <el-table-column prop="institute" label="所属学院" width="150" align="center" show-overflow-tooltip />
        <el-table-column prop="sex" label="性别" width="80" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.sex === '男' ? '' : 'success'" size="mini">
              {{ scope.row.sex }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="职称" width="100" align="center">
          <template slot-scope="scope">
            <el-tag 
              :type="getTypeTagType(scope.row.type)" 
              size="mini">
              {{ scope.row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tel" label="联系方式" width="130" align="center" />
        <el-table-column prop="email" label="邮箱" min-width="180" align="center" show-overflow-tooltip />
        <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-edit"
              @click="checkGrade(scope.row.teacherId)"
            >修改</el-button>
            <el-button
              size="mini"
              type="text"
              icon="el-icon-delete"
              @click="deleteById(scope.row)"
              style="color: #f56c6c"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination
        @current-change="handleCurrentChange"
        :current-page="pagination.current"
        :page-size="pagination.size"
        layout="total, prev, pager, next, jumper"
        :total="pagination.total"
        class="ruoyi-pagination">
      </el-pagination>
    </div>
    <!-- 编辑对话框-->
    <el-dialog
      title="编辑教师信息"
      :visible.sync="dialogVisible"
      width="30%"
      :before-close="handleClose">
      <section class="update">
        <el-form ref="form" :model="form" label-width="80px">
          <el-form-item label="姓名">
            <el-input v-model="form.teacherName"></el-input>
          </el-form-item>
          <el-form-item label="学院">
            <el-input v-model="form.institute"></el-input>
          </el-form-item>
          <el-form-item label="性别">
            <el-input v-model="form.sex"></el-input>
          </el-form-item>
          <el-form-item label="电话号码">
            <el-input v-model="form.tel"></el-input>
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.pwd"></el-input>
          </el-form-item>
          <el-form-item label="身份证号">
            <el-input v-model="form.cardId"></el-input>
          </el-form-item>
          <el-form-item label="职称">
            <el-input v-model="form.type"></el-input>
          </el-form-item>
        </el-form>
      </section>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submit()">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 查询参数
      queryParams: {
        teacherName: null,
        institute: null,
        type: null
      },
      pagination: {
        //分页后的考试信息
        current: 1, //当前页
        total: 0, //记录条数
        size: 10, //每页条数
        records: []
      },
      dialogVisible: false, //对话框
      form: {}, //保存点击以后当前试卷的信息
    };
  },
  created() {
    this.getTeacherInfo();
  },
  methods: {
    /** 查询教师列表 */
    getList() {
      this.loading = true;
      this.getTeacherInfo();
    },
    getTeacherInfo() {
      //分页查询所有教师信息
      this.loading = true;
      this.$axios(`/api/teachers/${this.pagination.current}/${this.pagination.size}`).then(res => {
        this.pagination = res.data.data;
        this.loading = false;
      }).catch(error => {
        this.loading = false;
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.pagination.current = 1;
      this.getTeacherInfo();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.$refs.queryForm.resetFields();
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.$router.push({path: '/addTeacher'});
    },
    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.teacherId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** 批量删除操作 */
    handleDelete() {
      const teacherIds = this.ids;
      this.$modal.confirm('是否确认删除教师编号为"' + teacherIds + '"的数据项？').then(() => {
        // 批量删除逻辑
        this.$message.success("删除成功");
        this.getList();
      });
    },
    /** 职称标签类型 */
    getTypeTagType(type) {
      const typeMap = {
        '教授': 'danger',
        '副教授': 'warning', 
        '讲师': '',
        '助教': 'info'
      };
      return typeMap[type] || 'info';
    },
    //改变当前记录条数
    handleSizeChange(val) {
      this.pagination.size = val;
      this.getTeacherInfo();
    },
    //改变当前页码，重新发送请求
    handleCurrentChange(val) {
      this.pagination.current = val;
      this.getTeacherInfo();
    },
    checkGrade(teacherId) { //修改教师信息
      this.dialogVisible = true
      this.$axios(`/api/teacher/${teacherId}`).then(res => {
        this.form = res.data.data
      })
    },
    deleteById(row) { //删除当前教师
      this.$confirm(`确定删除教师"${row.teacherName}"吗？删除后无法恢复!`, "警告", {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => { //确认删除
        this.$axios({
          url: `/api/teacher/${row.teacherId}`,
          method: 'delete',
        }).then(res => {
          if (res.data.code === 200) {
            this.$message.success("删除成功");
            this.getTeacherInfo();
          } else {
            this.$message.error(res.data.message || "删除失败");
          }
        }).catch(error => {
          this.$message.error("网络错误，请稍后重试");
        });
      }).catch(() => {
        this.$message.info("已取消删除");
      });
    },
    submit() { //提交更改
      this.dialogVisible = false
      this.$axios({
        url: '/api/teacher',
        method: 'put',
        data: {
          ...this.form
        }
      }).then(res => {
        console.log(res)
        if(res.data.code ==200) {
          this.$message({
            message: '更新成功',
            type: 'success'
          })
        }
        this.getTeacherInfo()
      })
    },
    handleClose(done) { //关闭提醒
      this.$confirm('确认关闭？')
        .then(_ => {
          done();
        }).catch(_ => {});
    },
  }
};
</script>
<style lang="less" scoped>
/* 若依风格教师管理页面样式 */
.ruoyi-container {
  padding: 20px;
  background-color: #f0f2f5;
}

.ruoyi-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  overflow: hidden;
}

/* 搜索表单样式 */
.search-card {
  padding: 20px;
}

.search-form .el-form-item {
  margin-bottom: 16px;
  margin-right: 16px;
}

.search-form .el-form-item__label {
  font-weight: 500;
  color: #606266;
}

/* 工具栏样式 */
.toolbar-card {
  padding: 16px 20px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 表格样式 */
.table-card {
  padding: 0;
}

.ruoyi-table {
  width: 100%;
  
  .el-table__header-wrapper {
    background-color: #fafafa;
  }
  
  .el-table__header th {
    background-color: #fafafa;
    color: #303133;
    font-weight: 500;
    border-bottom: 1px solid #ebeef5;
  }
  
  .el-table__row:hover > td {
    background-color: #f5f7fa;
  }
  
  .el-table__body tr td {
    border-bottom: 1px solid #ebeef5;
  }
}

/* 标签样式优化 */
.el-tag {
  border: none;
  border-radius: 12px;
  font-size: 12px;
  padding: 2px 8px;
}

/* 操作按钮样式 */
.el-button--text {
  padding: 0;
  margin-right: 12px;
  font-size: 12px;
  color: #409eff;
  
  &:hover {
    color: #66b1ff;
    background: none;
  }
  
  &:last-child {
    margin-right: 0;
  }
}

/* 分页样式 */
.ruoyi-pagination {
  padding: 20px;
  background-color: #fff;
  text-align: center;
  border-top: 1px solid #ebeef5;
}

.el-pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.el-pagination button:disabled {
  color: #c0c4cc;
  cursor: not-allowed;
}

.el-pagination .el-pager li.active {
  color: #409eff;
  background-color: #ecf5ff;
  border-color: #b3d8ff;
}

/* 对话框样式优化 */
.el-dialog {
  border-radius: 8px;
  
  .el-dialog__header {
    padding: 20px 24px 16px;
    border-bottom: 1px solid #e6e6e6;
    background-color: #fafbfc;
  }
  
  .el-dialog__title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
  
  .el-dialog__body {
    padding: 24px;
  }
  
  .el-dialog__footer {
    padding: 16px 24px 24px;
    border-top: 1px solid #e6e6e6;
    text-align: center;
  }
}

.update .el-form-item {
  margin-bottom: 20px;
}

.update .el-form-item__label {
  font-weight: 500;
  color: #606266;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .toolbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .search-form {
    flex-direction: column;
  }
  
  .search-form .el-form-item {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .ruoyi-container {
    padding: 10px;
  }
  
  .ruoyi-card {
    margin-bottom: 10px;
  }
  
  .search-card,
  .toolbar-card {
    padding: 16px;
  }
  
  .el-table {
    font-size: 12px;
  }
  
  .ruoyi-pagination {
    padding: 16px;
  }
}

/* 加载状态 */
.el-loading-mask {
  background-color: rgba(255, 255, 255, 0.9);
}

/* 权限指令样式 */
[v-hasPermi] {
  transition: all 0.3s;
}

/* 空数据样式 */
.el-table__empty-block {
  background-color: #fafafa;
}
</style>
