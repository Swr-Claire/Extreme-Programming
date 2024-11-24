<template>
  <div>
    <!-- 搜索栏 -->
    <el-card id="search">
      <el-row>
        <el-col :span="18">
          <el-input
            v-model="searchModel.username"
            placeholder="姓名"
            clearable
          ></el-input>
          <el-select
            v-model="searchModel.isFrequent"
            filterable
            placeholder="是否常用联系人"
          >
            <el-option label="是" value="1"> </el-option>
            <el-option label="否" value="0"> </el-option>
          </el-select>
          <el-button
            @click="getUserList"
            type="primary"
            round
            icon="el-icon-search"
            >查询</el-button
          >
          <el-button @click="resetForm()" round>重置</el-button>
        </el-col>
        <el-col :span="6" align="right">
          <el-button
            @click="openEditUI(null)"
            type="primary"
            icon="el-icon-plus"
            >新增</el-button
          >
          <el-button
            type="info"
            plain
            icon="el-icon-upload2"
            @click="handleImport"
            >导入</el-button
          >
          <el-button
            type="warning"
            plain
            icon="el-icon-download"
            @click="downloadFile"
            >导出</el-button
          >
        </el-col>
      </el-row>
    </el-card>

    <!-- 结果列表 -->
    <el-card>
      <el-table
        :data="userList"
        stripe
        style="width: 110%; height: calc(100vh - 290px)"
      >
        <el-table-column label="#" width="80">
          <template slot-scope="scope">
            <!-- (pageNo-1) * pageSize + index + 1 -->
            {{
              (searchModel.pageNo - 1) * searchModel.pageSize + scope.$index + 1
            }}
          </template>
        </el-table-column>
        <el-table-column prop="id" label="用户ID"> </el-table-column>
        <el-table-column prop="username" label="姓名"> </el-table-column>
        <el-table-column prop="status" label="用户状态">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.status == 1">正常</el-tag>
            <el-tag v-else type="danger">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isFrequent" label="是否常用联系人">
          <template slot-scope="scope">
            <!-- <el-tag v-if="scope.row.isFrequent == 1">是</el-tag>
            <el-tag v-else type="danger">否</el-tag> -->
            <el-switch
              v-model="scope.row.isFrequent"
              :active-value="1"
              :inactive-value="0"
              @change="updateUserFrequent(scope.row.id)"
            >
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="联系方式" width="500">
          <template slot-scope="scope">
            {{
              scope.row.userContactDetails &&
              userContactName(scope.row.userContactDetails)
            }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button
              @click="openEditUI(scope.row.id)"
              type="primary"
              icon="el-icon-edit"
              size="mini"
              circle
            ></el-button>
            <el-button
              @click="deleteUser(scope.row)"
              type="danger"
              icon="el-icon-delete"
              size="mini"
              circle
            ></el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分页组件 -->
    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="searchModel.pageNo"
      :page-sizes="[5, 10, 20, 50]"
      :page-size="searchModel.pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
    >
    </el-pagination>

    <!-- 用户信息编辑对话框 -->
    <el-dialog
      width="600px"
      @close="clearForm"
      :title="title"
      :visible.sync="dialogFormVisible"
    >
      <el-form :model="userForm" ref="userFormRef" :rules="rules">
        <el-form-item
          label="姓名"
          prop="username"
          :label-width="formLabelWidth"
        >
          <el-input v-model="userForm.username" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item
          v-if="userForm.id == null || userForm.id == undefined"
          label="登录密码"
          prop="password"
          :label-width="formLabelWidth"
        >
          <el-input
            type="password"
            v-model="userForm.password"
            autocomplete="off"
          ></el-input>
        </el-form-item>
        <el-form-item label="用户状态" :label-width="formLabelWidth">
          <el-switch
            v-model="userForm.status"
            :active-value="1"
            :inactive-value="0"
          >
          </el-switch>
        </el-form-item>
        <el-form-item label="是否常用联系人" :label-width="formLabelWidth">
          <el-switch
            v-model="userForm.isFrequent"
            :active-value="1"
            :inactive-value="0"
          >
          </el-switch>
        </el-form-item>
        <el-button type="primary" plain class="addItemBtn" @click="addItem"
          >增加联系方式</el-button
        >
        <div
          class="inline"
          v-for="(userContact, index) in userForm.userContactDetails"
          :key="index"
        >
          <el-form-item :label="'联系方式类型' + (index + 1)">
            <el-input
              v-model="userContact.name"
              placeholder="请选择联系方式类型"
              autocomplete="off"
            ></el-input>
          </el-form-item>
          <el-form-item :label="'联系方式详情' + (index + 1)">
            <el-input
              v-model="userContact.detail"
              placeholder="请选择联系方式详情"
              autocomplete="off"
            ></el-input>
          </el-form-item>
          <el-link type="danger" @click="delItem(index)">删除</el-link>
        </div>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="saveUser">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog
      :title="upload.title"
      :visible.sync="upload.open"
      width="400px"
      append-to-body
      class="el-upload-dialog"
    >
      <el-upload
        ref="upload"
        :limit="1"
        accept=".xlsx, .xls"
        :headers="upload.headers"
        :action="'http://36.137.185.208:8204/prod-api/user/importData'"
        :disabled="upload.isUploading"
        :on-progress="handleFileUploadProgress"
        :on-success="handleFileSuccess"
        :auto-upload="false"
        drag
      >
        <i class="el-icon-upload" />
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <div>
          <el-button type="primary" @click="importTemplate">导入模板</el-button>
        </div>
        <div>
          <el-button type="primary" @click="submitFileForm">确 定</el-button>
          <el-button @click="upload.open = false">取 消</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import userApi from "@/api/userManage";
import { getToken } from "@/utils/auth";
export default {
  data() {
    var checkEmail = (rule, value, callback) => {
      var reg =
        /^[a-zA-Z0-9]+([-_.][a-zA-Z0-9]+)*@[a-zA-Z0-9]+([-_.][a-zA-Z0-9]+)*\.[a-z]{2,}$/;
      if (!reg.test(value)) {
        return callback(new Error("邮箱格式错误"));
      }
      callback();
    };
    return {
      formLabelWidth: "110px",
      userForm: {
        userContactDetails: [{ name: "", detail: "" }],
      },
      dialogFormVisible: false,
      title: "",
      total: 0,
      searchModel: {
        pageNo: 1,
        pageSize: 10,
      },
      userList: [],
      rules: {
        username: [
          { required: true, message: "请输入用户名", trigger: "blur" },
          {
            min: 3,
            max: 50,
            message: "长度在 3 到 50 个字符",
            trigger: "blur",
          },
        ],
        password: [
          { required: true, message: "请输入登录初始密码", trigger: "blur" },
          {
            min: 6,
            max: 16,
            message: "长度在 6 到 16 个字符",
            trigger: "blur",
          },
        ],
        email: [
          { required: true, message: "请输入电子邮件", trigger: "blur" },
          { validator: checkEmail, trigger: "blur" },
        ],
      },
      Loading: false,
      // 家庭地址列表
      homeAddressList: ["公司地址", "家庭地址"],
      // 公司地址列表
      companyAddressList: ["福建省福州市软件园", "福州市西湖"],
      // 导入参数
      upload: {
        // 是否显示弹出层（导入）
        open: false,
        // 弹出层标题（导入）
        title: "",
        // 是否禁用上传
        isUploading: false,
        // 是否更新已经存在的用户数据
        updateSupport: 0,
        // 设置上传的请求头部
        headers: { Authorization: "Bearer " + getToken() },
        // 上传的地址
        url: process.env.VUE_APP_BASE_API + "/system/user/importData",
      },
    };
  },
  methods: {
    // 重置
    resetForm() {
      this.searchModel.pageNo = 1;
      this.searchModel.pageSize = 10;
      this.searchModel.username = "";
      this.searchModel.phone = "";
      this.searchModel.isFrequent = "";
      this.getUserList();
    },
    // 联系方式类型
    userContactName(userContactDetails) {
      let arr = [];
      userContactDetails &&
        userContactDetails.map((item) => {
          arr.push(item.name + ":" + item.detail);
        });
      return arr.join(",");
    },

    // 增加联系方式
    addItem() {
      this.userForm.userContactDetails.push({ name: "", detail: "" });
      console.log(this.userForm.userContactDetails);
    },
    // 删除指标库
    delItem(index) {
      this.userForm.userContactDetails.splice(index, 1);
    },
    /** 导入按钮操作 */
    handleImport() {
      this.upload.title = "导入";
      this.upload.open = true;
    },
    /** 下载模板操作 */
    async importTemplate() {
      let res = await userApi.downloadTemplate();
      let blob = res;
      let content = [];
      let fileName = "下载模板.xlsx";
      content.push(blob);
      this.downloadExcelFile(content, fileName);
    },
    // 文件上传中处理
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true;
    },
    // 文件上传成功处理
    handleFileSuccess(response, file, fileList) {
      this.upload.open = false;
      this.upload.isUploading = false;
      this.$refs.upload.clearFiles();
      this.getUserList();
    },
    // 提交上传文件
    submitFileForm() {
      this.$refs.upload.submit();
    },
    // 是否常用联系人更新
    updateUserFrequent(id) {
      userApi.updateUserFrequent(id).then((response) => {
        this.$message({
          type: "success",
          message: response.message,
        });
        // this.getUserList();
      });
    },
    //   导出模板
    async downloadFile() {
      // let params = {
      //   ...{ ids: idsParams },
      //   ...{ condition: this.form },
      //   ...{ selectCols: this.selectCols },
      // };
      let res = await userApi.downloadFile(this.searchModel);
      let blob = res;
      let content = [];
      let fileName = "导出模板.xlsx";
      content.push(blob);
      this.downloadExcelFile(content, fileName);
    },
    // 下载
    downloadExcelFile(data, fileName) {
      const url = window.URL.createObjectURL(new Blob(data));
      const link = document.createElement("a");
      link.style.display = "none";
      link.href = url;
      link.setAttribute("download", fileName);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    },
    deleteUser(user) {
      this.$confirm(`您确认删除用户 ${user.username} ?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          userApi.deleteUserById(user.id).then((response) => {
            this.$message({
              type: "success",
              message: response.message,
            });
            this.getUserList();
          });
        })
        .catch(() => {
          this.$message({
            type: "info",
            message: "已取消删除",
          });
        });
    },
    saveUser() {
      // 触发表单验证
      this.$refs.userFormRef.validate((valid) => {
        if (valid) {
          // 提交请求给后台
          userApi.saveUser(this.userForm).then((response) => {
            // 成功提示
            this.$message({
              message: response.message,
              type: "success",
            });
            // 关闭对话框
            this.dialogFormVisible = false;
            // 刷新表格
            this.getUserList();
          });
        } else {
          console.log("error submit!!");
          return false;
        }
      });
    },
    clearForm() {
      this.userForm = {};
      this.$refs.userFormRef.clearValidate();
    },
    openEditUI(id) {
      if (id == null) {
        this.title = "新增联系人";
      } else {
        this.title = "修改用户";
        // 根据id查询用户数据
        userApi.getUserById(id).then((response) => {
          this.userForm = response.data;
        });
      }
      this.dialogFormVisible = true;
    },
    handleSizeChange(pageSize) {
      this.searchModel.pageSize = pageSize;
      this.getUserList();
    },
    handleCurrentChange(pageNo) {
      this.searchModel.pageNo = pageNo;
      this.getUserList();
    },
    getUserList() {
      userApi.getUserList(this.searchModel).then((response) => {
        this.userList = response.data.rows;
        this.total = response.data.total;
      });
    },
  },
  created() {
    this.getUserList();
  },
};
</script>

<style>
#search .el-input {
  width: 200px;
  margin-right: 10px;
}
.el-dialog .el-input {
  width: 95%;
}
.el-pagination {
  text-align: right;
}
.addItemBtn {
  margin-left: 110px;
  margin-bottom: 20px;
}
.inline {
  margin-left: 110px;
}
.inline .el-form-item {
  display: inline-block;
}
.el-upload-dialog .dialog-footer {
  display: flex;
  justify-content: space-between;
}
</style>
