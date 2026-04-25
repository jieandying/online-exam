<template>
  <div class="ruoyi-container ai-question-generate">
    <!-- 功能选择卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="12">
        <el-card class="function-card" :class="{active: generateMode === 'prompt'}" 
                 @click.native="generateMode = 'prompt'">
          <div class="card-content">
            <i class="el-icon-edit-outline card-icon"></i>
            <h3>提示词出题</h3>
            <p>通过输入提示词和要求，AI自动生成题目</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="function-card" :class="{active: generateMode === 'document'}" 
                 @click.native="generateMode = 'document'">
          <div class="card-content">
            <i class="el-icon-upload card-icon"></i>
            <h3>文档出题</h3>
            <p>上传Word、PDF、Excel等文档，基于文档内容生成题目</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 出题配置表单 -->
    <el-card class="form-card">
      <div slot="header">
        <span>出题配置</span>
      </div>
      
      <el-form :model="questionForm" :rules="formRules" ref="questionForm" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="科目" prop="subject">
              <el-select v-model="questionForm.subject" placeholder="选择科目" style="width: 100%">
                <el-option v-for="subject in subjects" :key="subject" :label="subject" :value="subject"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="题目类型" prop="questionType">
              <el-select v-model="questionForm.questionType" placeholder="选择题目类型" style="width: 100%">
                <el-option v-for="(label, value) in questionTypes" :key="value" :label="label" :value="value"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="题目数量" prop="questionCount">
              <el-select v-model="questionForm.questionCount" placeholder="请选择题目数量" style="width:100%">
                <el-option v-for="n in [5,6,7,8,9,10]" :key="n" :label="n + ' 道'" :value="n" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度等级" prop="difficulty">
              <el-select v-model="questionForm.difficulty" placeholder="选择难度等级" style="width: 100%">
                <el-option v-for="(label, value) in difficulties" :key="value" :label="label" :value="value"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="章节" prop="chapter">
          <el-input v-model="questionForm.chapter" placeholder="请输入章节名称（可选）"/>
        </el-form-item>
        
        <!-- 提示词模式 -->
        <div v-if="generateMode === 'prompt'">
          <el-form-item label="提示词" prop="prompt">
            <el-input type="textarea" v-model="questionForm.prompt" :rows="4"
                     placeholder="请输入出题要求，例如：生成关于数据结构中栈和队列的题目，重点考查基本概念和应用"/>
          </el-form-item>
          
          <el-form-item label="附加要求">
            <el-input type="textarea" v-model="questionForm.requirements" :rows="2"
                     placeholder="其他要求（可选）"/>
          </el-form-item>
        </div>
        
        <!-- 文档模式 -->
        <div v-if="generateMode === 'document'">
          <el-form-item label="上传文档" prop="document">
            <el-upload
              class="document-uploader"
              drag
              action=""
              :before-upload="handleDocumentUpload"
              :show-file-list="false"
              accept=".pdf,.docx,.xlsx,.txt">
              <div v-if="!uploadFile">
                <i class="el-icon-upload"></i>
                <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
                <div class="el-upload__tip">支持PDF、Word、Excel、TXT格式，文件大小不超过10MB</div>
              </div>
              <div v-else class="uploaded-file">
                <i class="el-icon-document"></i>
                <span>{{ uploadFile.name }}</span>
                <el-button type="text" @click="removeFile" style="margin-left: 10px;">删除</el-button>
              </div>
            </el-upload>
          </el-form-item>
          
          <el-form-item label="补充要求">
            <el-input type="textarea" v-model="questionForm.prompt" :rows="3"
                     placeholder="基于文档内容的补充要求（可选）"/>
          </el-form-item>
        </div>
        
        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" @click="generateQuestions" :loading="generating" size="medium">
            <i class="el-icon-magic-stick"></i> 
            {{ generating ? '生成中...' : 'AI生成题目' }}
          </el-button>
          <el-button @click="resetForm" size="medium">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 生成结果 -->
    <el-card v-if="generatedQuestions.length > 0" class="result-card">
      <div slot="header">
        <span>生成结果 ({{ generatedQuestions.length }}道题目)</span>
        <div style="float: right;">
          <el-button type="success" size="small" @click="saveAllQuestions" :loading="saving">
            <i class="el-icon-check"></i> 保存所有题目
          </el-button>
          <el-button type="info" size="small" @click="clearResults">
            <i class="el-icon-delete"></i> 清空结果
          </el-button>
        </div>
      </div>
      
      <!-- 题目列表 -->
      <div class="questions-list">
        <div v-for="(question, index) in generatedQuestions" :key="index" class="question-item">
          <div class="question-header">
            <span class="question-number">第{{ index + 1 }}题</span>
            <span class="question-type-tag">{{ getQuestionTypeName(questionForm.questionType) }}</span>
          </div>
          
          <!-- 选择题显示 -->
          <div v-if="questionForm.questionType === 'multiple'" class="question-content">
            <div class="question-title">{{ question.question }}</div>
            <div class="question-options">
              <div class="option-item">A. {{ question.answerA }}</div>
              <div class="option-item">B. {{ question.answerB }}</div>
              <div class="option-item">C. {{ question.answerC }}</div>
              <div class="option-item">D. {{ question.answerD }}</div>
            </div>
            <div class="question-answer">正确答案：{{ question.rightAnswer }}</div>
            <div class="question-analysis">解析：{{ question.analysis }}</div>
          </div>
          
          <!-- 填空题显示 -->
          <div v-else-if="questionForm.questionType === 'fill'" class="question-content">
            <div class="question-title">{{ question.question }}</div>
            <div class="question-answer">参考答案：{{ question.answer }}</div>
            <div class="question-analysis">解析：{{ question.analysis }}</div>
          </div>
          
          <!-- 判断题显示 -->
          <div v-else-if="questionForm.questionType === 'judge'" class="question-content">
            <div class="question-title">{{ question.question }}</div>
            <div class="question-answer">正确答案：{{ question.answer === 'T' ? '正确' : '错误' }}</div>
            <div class="question-analysis">解析：{{ question.analysis }}</div>
          </div>
          
          <!-- 主观题显示 -->
          <div v-else-if="questionForm.questionType === 'subjective'" class="question-content">
            <div class="question-title">{{ question.question }}</div>
            <div class="question-answer subjective-answer">
              <div class="answer-label">参考答案：</div>
              <div class="answer-content">{{ question.answer }}</div>
            </div>
            <div class="question-score" v-if="question.score">分值：{{ question.score }}分</div>
            <div class="question-analysis">评分要点：{{ question.analysis }}</div>
          </div>
          
          <!-- 题目操作 -->
          <div class="question-actions">
            <el-button type="text" size="small" @click="editQuestion(index)">
              <i class="el-icon-edit"></i> 编辑
            </el-button>
            <el-button type="text" size="small" @click="deleteQuestion(index)" style="color: #f56c6c;">
              <i class="el-icon-delete"></i> 删除
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 编辑题目对话框 -->
    <el-dialog :title="'编辑' + getQuestionTypeName(questionForm.questionType)" :visible.sync="editDialogVisible" width="60%">
      <el-form :model="editingQuestion" label-width="100px" v-if="editingQuestion">
        <el-form-item label="题目内容">
          <el-input type="textarea" v-model="editingQuestion.question" :rows="3"/>
        </el-form-item>
        
        <!-- 选择题编辑 -->
        <div v-if="questionForm.questionType === 'multiple'">
          <el-form-item label="选项A">
            <el-input v-model="editingQuestion.answerA"/>
          </el-form-item>
          <el-form-item label="选项B">
            <el-input v-model="editingQuestion.answerB"/>
          </el-form-item>
          <el-form-item label="选项C">
            <el-input v-model="editingQuestion.answerC"/>
          </el-form-item>
          <el-form-item label="选项D">
            <el-input v-model="editingQuestion.answerD"/>
          </el-form-item>
          <el-form-item label="正确答案">
            <el-radio-group v-model="editingQuestion.rightAnswer">
              <el-radio label="A">A</el-radio>
              <el-radio label="B">B</el-radio>
              <el-radio label="C">C</el-radio>
              <el-radio label="D">D</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
        
        <!-- 填空题编辑 -->
        <el-form-item v-else-if="questionForm.questionType === 'fill'" label="答案">
          <el-input v-model="editingQuestion.answer"/>
        </el-form-item>
        
        <!-- 判断题编辑 -->
        <el-form-item v-else-if="questionForm.questionType === 'judge'" label="答案">
          <el-radio-group v-model="editingQuestion.answer">
            <el-radio label="T">正确</el-radio>
            <el-radio label="F">错误</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 主观题编辑 -->
        <div v-else-if="questionForm.questionType === 'subjective'">
          <el-form-item label="参考答案">
            <el-input type="textarea" v-model="editingQuestion.answer" :rows="5" placeholder="请输入参考答案"/>
          </el-form-item>
          <el-form-item label="分值">
            <el-input-number v-model="editingQuestion.score" :min="1" :max="100" placeholder="题目分值"/>
          </el-form-item>
        </div>
        
        <el-form-item :label="questionForm.questionType === 'subjective' ? '评分要点' : '解析'">
          <el-input type="textarea" v-model="editingQuestion.analysis" :rows="3"/>
        </el-form-item>
      </el-form>
      
      <div slot="footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEditedQuestion">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'AIQuestionGenerateAdmin',
  data() {
    return {
      generateMode: 'prompt', // prompt | document
      generating: false,
      saving: false,
      uploadFile: null,
      editDialogVisible: false,
      editingQuestion: null,
      editingIndex: -1,
      
      // 表单数据
      questionForm: {
        subject: '',
        questionType: '',
        questionCount: 5,
        difficulty: '2',
        chapter: '',
        prompt: '',
        requirements: ''
      },
      
      // 验证规则
      formRules: {
        subject: [{ required: true, message: '请选择科目', trigger: 'change' }],
        questionType: [{ required: true, message: '请选择题目类型', trigger: 'change' }],
        questionCount: [{ required: true, message: '请输入题目数量', trigger: 'blur' }],
        prompt: [
          { required: true, message: '请输入提示词', trigger: 'blur' },
          { min: 10, message: '提示词至少10个字符', trigger: 'blur' }
        ]
      },
      
      // 选项数据
      subjects: [],
      questionTypes: {},
      difficulties: {},
      
      // 生成结果
      generatedQuestions: []
    }
  },
  
  created() {
    this.loadBasicData()
  },
  
  methods: {
    // 加载基础数据
    async loadBasicData() {
      try {
        // 加载科目列表
        const subjectsRes = await this.$axios.get('/api/ai/subjects')
        if (subjectsRes.data.code === 200) {
          this.subjects = subjectsRes.data.data
        }
        
        // 加载题目类型
        const typesRes = await this.$axios.get('/api/ai/questionTypes')
        if (typesRes.data.code === 200) {
          this.questionTypes = typesRes.data.data
        }
        
        // 加载难度等级
        const difficultiesRes = await this.$axios.get('/api/ai/difficulties')
        if (difficultiesRes.data.code === 200) {
          this.difficulties = difficultiesRes.data.data
        }
      } catch (error) {
        this.$message.error('加载基础数据失败：' + (error.response && error.response.data && error.response.data.message ? error.response.data.message : error.message))
      }
    },
    
    // 处理文档上传
    handleDocumentUpload(file) {
      // 验证文件类型
      const validTypes = ['application/pdf', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 
                         'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'text/plain']
      if (!validTypes.includes(file.type)) {
        this.$message.error('只支持PDF、Word、Excel、TXT格式的文件')
        return false
      }
      
      // 验证文件大小
      if (file.size > 10 * 1024 * 1024) {
        this.$message.error('文件大小不能超过10MB')
        return false
      }
      
      this.uploadFile = file
      return false // 阻止自动上传
    },
    
    // 删除上传的文件
    removeFile() {
      this.uploadFile = null
    },
    
    // 生成题目
    async generateQuestions() {
      // 表单验证
      try {
        await this.$refs.questionForm.validate()
      } catch (error) {
        return
      }
      
      // 特殊验证
      if (this.generateMode === 'prompt' && !this.questionForm.prompt.trim()) {
        this.$message.error('请输入提示词')
        return
      }
      
      if (this.generateMode === 'document' && !this.uploadFile) {
        this.$message.error('请上传文档')
        return
      }
      
      this.generating = true
      
      try {
        let response
        
        if (this.generateMode === 'prompt') {
          // 基于提示词生成
          response = await this.$axios.post('/api/ai/generateByPrompt', this.questionForm)
        } else {
          // 基于文档生成
          const formData = new FormData()
          formData.append('file', this.uploadFile)
          formData.append('subject', this.questionForm.subject)
          formData.append('questionType', this.questionForm.questionType)
          formData.append('questionCount', this.questionForm.questionCount.toString())
          formData.append('difficulty', this.questionForm.difficulty)
          if (this.questionForm.chapter) formData.append('chapter', this.questionForm.chapter)
          if (this.questionForm.prompt) formData.append('prompt', this.questionForm.prompt)
          if (this.questionForm.requirements) formData.append('requirements', this.questionForm.requirements)
          
          response = await this.$axios.post('/api/ai/generateByDocument', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          })
        }
        
        if (response.data.code === 200) {
          this.generatedQuestions = response.data.data.questions || []
          this.$message.success(`成功生成${this.generatedQuestions.length}道题目`)
          // 通知父组件题目已生成
          this.$emit('questions-generated', this.generatedQuestions)
        } else {
          this.$message.error(response.data.message || '生成题目失败')
        }
      } catch (error) {
        this.$message.error('生成题目失败：' + (error.response && error.response.data && error.response.data.message ? error.response.data.message : error.message))
      } finally {
        this.generating = false
      }
    },
    
    // 保存所有题目
    async saveAllQuestions() {
      if (this.generatedQuestions.length === 0) {
        this.$message.warning('没有可保存的题目')
        return
      }
      
      this.saving = true
      
      try {
        const response = await this.$axios.post('/api/ai/saveQuestions', {
          questions: this.generatedQuestions,
          questionType: this.questionForm.questionType
        })
        
        if (response.data.code === 200) {
          this.$message.success('题目保存成功')
          this.clearResults()
          // 通知父组件题目已保存
          this.$emit('questions-saved', this.generatedQuestions)
        } else {
          this.$message.error(response.data.message || '题目保存失败')
        }
      } catch (error) {
        this.$message.error('保存题目失败：' + (error.response && error.response.data && error.response.data.message ? error.response.data.message : error.message))
      } finally {
        this.saving = false
      }
    },
    
    // 编辑题目
    editQuestion(index) {
      this.editingIndex = index
      this.editingQuestion = JSON.parse(JSON.stringify(this.generatedQuestions[index]))
      this.editDialogVisible = true
    },
    
    // 保存编辑的题目
    saveEditedQuestion() {
      this.generatedQuestions[this.editingIndex] = this.editingQuestion
      this.editDialogVisible = false
      this.$message.success('题目修改成功')
    },
    
    // 删除题目
    deleteQuestion(index) {
      this.$confirm('确定要删除这道题目吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.generatedQuestions.splice(index, 1)
        this.$message.success('题目删除成功')
      })
    },
    
    // 清空结果
    clearResults() {
      this.generatedQuestions = []
    },
    
    // 重置表单
    resetForm() {
      this.$refs.questionForm.resetFields()
      this.uploadFile = null
      this.generatedQuestions = []
    },
    
    // 获取题目类型名称
    getQuestionTypeName(type) {
      return this.questionTypes[type] || type
    }
  }
}
</script>

<style scoped>
.ai-question-generate {
  padding: 20px;
  background-color: #f0f2f5;
}

.function-card {
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
  border-radius: 8px;
}

.function-card:hover {
  border-color: #409EFF;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.function-card.active {
  border-color: #409EFF;
  background-color: #f0f8ff;
}

.card-content {
  text-align: center;
  padding: 20px;
}

.card-icon {
  font-size: 48px;
  color: #409EFF;
  margin-bottom: 15px;
}

.card-content h3 {
  margin: 0 0 10px 0;
  color: #303133;
}

.card-content p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.form-card, .result-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.document-uploader {
  width: 100%;
}

.uploaded-file {
  padding: 20px;
  text-align: center;
  color: #606266;
}

.questions-list {
  max-height: 600px;
  overflow-y: auto;
}

.question-item {
  border: 1px solid #EBEEF5;
  border-radius: 6px;
  padding: 20px;
  margin-bottom: 15px;
  background-color: #FAFAFA;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.question-number {
  font-weight: bold;
  color: #409EFF;
  font-size: 16px;
}

.question-type-tag {
  background-color: #E1F3D8;
  color: #67C23A;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
}

.question-content {
  margin-bottom: 15px;
}

.question-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #303133;
  line-height: 1.5;
}

.question-options {
  margin: 10px 0;
}

.option-item {
  padding: 5px 0;
  color: #606266;
}

.question-answer {
  margin: 10px 0;
  padding: 8px 15px;
  background-color: #E8F4FD;
  border-left: 3px solid #409EFF;
  color: #409EFF;
  font-weight: bold;
}

.question-analysis {
  margin: 10px 0;
  padding: 8px 15px;
  background-color: #FEF7E8;
  border-left: 3px solid #E6A23C;
  color: #E6A23C;
  line-height: 1.5;
}

/* 主观题样式 */
.subjective-answer {
  background-color: #F0F9EB;
  border-left: 3px solid #67C23A;
  color: #303133;
  font-weight: normal;
}

.subjective-answer .answer-label {
  font-weight: bold;
  color: #67C23A;
  margin-bottom: 8px;
}

.subjective-answer .answer-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #606266;
}

.question-score {
  margin: 10px 0;
  padding: 8px 15px;
  background-color: #FDF6EC;
  border-left: 3px solid #E6A23C;
  color: #E6A23C;
  font-weight: bold;
}

.question-actions {
  text-align: right;
  border-top: 1px solid #EBEEF5;
  padding-top: 10px;
}

/* 美化计数器样式（与遗传算法组卷一致） */
/deep/ .el-input-number {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  width: 100%;

  &:hover {
    border-color: #409eff;
  }

  .el-input__inner {
    border: none !important;
    text-align: center;
    padding-left: 50px;
    padding-right: 50px;
  }

  .el-input-number__decrease,
  .el-input-number__increase {
    background: #f5f7fa;
    border-right: 1px solid #dcdfe6;
    color: #606266;
    width: 40px;

    &:hover {
      color: #409eff;
      background: #ecf5ff;
    }
  }

  .el-input-number__increase {
    border-right: none;
    border-left: 1px solid #dcdfe6;
  }
}
</style>
