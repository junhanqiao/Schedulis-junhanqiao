<template>
  <div id="dep-relation-manage">
    <a-divider />
    <DepExecSearchForm :searchCallBack="searchCallBack">
      <a-table
        bordered
        :data-source="tableData"
        :columns="tableConf.columns"
        :rowKey="`id`"
        :pagination="false"
      >
        <template slot="status" slot-scope="text, record">
          <a-tag :color="getStatusColor(record.status)">{{record.status}}</a-tag>
        </template>
        <template slot="operation" slot-scope="text, record">
          <a-popconfirm v-if="tableData.length" title="确定重做?" @confirm="() => onRedo(record)">
            <a href="javascript:;">重做</a>
          </a-popconfirm>
          <a-divider type="vertical" />
          <a
            :href="'/reExeDepflow?execid='+record.execId+'&depInstId='+record.id"
            :disabled="record.status in ['INIT','READY']"
          >定制重做</a>          
          <a-divider type="vertical" />
          <a
            :href="'/executor?execid='+record.execId"
            :disabled="record.status in ['INIT','READY']"
          >执行详情</a>
        </template>
      </a-table>
    </DepExecSearchForm>
  </div>
</template>
<script>
import DepExecSearchForm from "./DepExecSearchForm";
import moment from "moment";
import services from "../services";
let tsFormat = "YYYY-MM-DD HH:mm:ss"
export default {
  name: "DepRelationManage",
  components: { DepExecSearchForm },
  data() {
    return {
      tableData: [],
      tableConf: {

        columns: [
          {
            title: "时间ID",
            dataIndex: "timeId",
            customRender: (text, row, index) => {
              let result = "";
              if (row.timeId) {
                result = moment(row.timeId).format("YYYY-MM-DD");
              }
              return result;
            }
          },
          {
            title: "项目",
            dataIndex: "projectName"
          },
          {
            title: "工作流",
            dataIndex: "flowId"
          },

          {
            title: "实例状态",
            dataIndex: "status",
            scopedSlots: { customRender: "status" }
          },
          {
            title: "创建时间",
            dataIndex: "createTime",
            customRender: (text, row, index) => {
              let result = "";
              if (row.createTime) {
                result = moment(row.createTime).format(tsFormat);
              }
              return result;
            }
          },
          {
            title: "修改时间",
            dataIndex: "modify_time",
            customRender: (text, row, index) => {
              let result = "";
              if (row.modifyTime) {
                result = moment(row.modifyTime).format(tsFormat);
              }
              return result;
            }
          },
          {
            title: "操作",
            dataIndex: "operation",
            scopedSlots: { customRender: "operation" }
          }
        ],
        expertColumns: [

          {
            title: "执行ID",
            dataIndex: "execId"
          },
          {
            title: "开始时间",
            dataIndex: "startTime"
          },
          {
            title: "结束时间",
            dataIndex: "endTime"
          },
          {
            title: "执行时长",
            dataIndex: "difftime"
          },
          {
            title: "执行状态",
            dataIndex: "exeStatus"
          }
        ],
        statusColor: {
          INIT: "grey",
          READY: "blue",
          SUBMITTED: "cyan",
          SUCCESS: "green",
          FAILED: "red"
        }
      }
    };
  },
  computed: {},
  methods: {
    searchCallBack(res) {
      this.tableData = res.data;
    },

    onRedo(key) {
      services.redoFlowInstance(
        key,
        res => {
          if (res.data.code) {
            this.$message.error(res.data.message);
            return;
          }
        },
        res => {
          this.$message.error("redo failed");
        }
      );
    },
    getStatusColor(status) {
      let color = this.tableConf.statusColor[status] ? this.tableConf.statusColor[status] : "grey";
      return color;
    }
  }
};
</script>
