<template>
  <a-modal
    :visible="visible"
    :title="'新增依赖'"
    okText="确认"
    cancel-text="取消"
    @cancel="handleCancel"
    @ok="handleOk"
    @afterClose="handleCancel"
  >
    <a-form class="dep-relation-add-form" :form="form">
      <a-row>
        <a-col :key="'depended_project_id'">
          <a-form-item :label="`前置项目`">
            <a-select
              show-search
              allowClear
              placeholder="input search text"
              :default-active-first-option="false"
              :show-arrow="false"
              :filter-option="false"
              :not-found-content="null"
              @search="handleDepedProjectSearch"
              @change="handleDepedProjectChange"
              v-decorator="['dependedProjectId']"
            >
              <a-select-option v-for="d in depedProjects" :key="d.value">{{ d.text }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :key="'depended_flow_id'">
          <a-form-item :label="`前置工作流`">
            <a-select
              show-search
              allowClear
              placeholder="input search text"
              :default-active-first-option="false"
              :show-arrow="false"
              :filter-option="false"
              :not-found-content="null"
              @search="handleDepedProjectSearch"
              @change="handleDepedProjectChange"
              v-decorator="['dependedFlowId']"
            >
              <a-select-option v-for="d in depedProjects" :key="d.value">{{ d.text }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>

      <a-row>
        <a-col :key="'project_id'" :span="4">
          <a-form-item :label="`项目`">
            <a-select
              show-search
              allowClear
              placeholder="input search text"
              :default-active-first-option="false"
              :show-arrow="false"
              :filter-option="false"
              :not-found-content="null"
              @search="handleProjectSearch"
              @change="handleProjectChange"
              v-decorator="['projectId']"
            >
              <a-select-option v-for="d in projects" :key="d.value">{{ d.text }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :key="'flow_id'" :span="4">
          <a-form-item :label="`工作流`">
            <a-select
              show-search
              allowClear
              placeholder="input search text"
              :default-active-first-option="false"
              :show-arrow="false"
              :filter-option="false"
              :not-found-content="null"
              @search="handleFlowSearch"
              v-decorator="['flowId']"
            >
              <a-select-option v-for="d in flows" :key="d.value">{{ d.text }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>      
    </a-form>
  </a-modal>
</template>
<script>
import services from "../services";
export default {
  name: "DepRelationAddForm",
  props:{
      visible:Boolean,
      okCallBack:Function,
      cancelCallBack:Function,
  },
  data() {
    return {
      form: this.$form.createForm(this, { name: "dep-relation-add-form" }),
      depedProjects: [],
      depedFlows: [],
      projects: [],
      flows: [],
    };
  },
  computed: {},
  methods: {
    handleDepedProjectSearch(value) {
      services.searchProjectByName(value, res => {
        this.depedProjects = res.data;
      });
    },
    handleDepedProjectChange(value, option) {
      console.log(value);
      console.log(option);
      //update depedFlows
      services.getFlowsByProject(value,res=>{this.depedFlows=res.data})
    },

    handleDepedFlowSearch(value,option) {
      console.log(value);
    },
    handleProjectSearch(value) {
      services.searchProjectByName(value, res => {
        this.depedProjects = res.data;
      });
    },
    handleProjectChange(value, option) {
      console.log(value);
      console.log(option);
      //update depedFlows
    },
    handleFlowSearch(value) {
      console.log(value);
    },
    handleOk(e) {
      e.preventDefault();
      this.form.validateFields((error, values) => {
        console.log("error", error);
        console.log("Received values of form: ", values);
      });
      services.searchDepRelations("", res => {
        this.tableData = res.data;
      });
      if(this.okCallBack){
          this.okCallBack()
      }
    },
    handleCancel(){
      if(this.cancelCallBack){
          this.cancelCallBack()
      }        
    }
  }
};
</script>
