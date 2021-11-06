<template>
  <div id="dep-relation-manage">
    <a-divider />    
    <DepExecSearchForm :searchCallBack='searchCallBack'>
        <a-table bordered :data-source="tableData" :columns="columns" :rowKey="`id`" :pagination='false'>
            <template slot="operation" slot-scope="text, record">
                <a-popconfirm
                v-if="tableData.length"
                title="确定重做?"
                @confirm="() => onRedo(record)"
                >
                <a href="javascript:;">重做</a>
                </a-popconfirm>
            </template>
        </a-table>
    </DepExecSearchForm>
  </div>
  
</template>
<script>
import DepExecSearchForm from './DepExecSearchForm'
import moment from 'moment'
import services from '../services';
export default {
  name: 'DepRelationManage',  
  components:{DepExecSearchForm},
  data() {
    return {
      tableData: [],
      columns: [
        {
          title: 'id',
          dataIndex: 'id',
          width: '30%',
          scopedSlots: { customRender: 'id' },
        },
        {
          title: '项目',
          dataIndex: 'projectName',
        },
        {
          title: '工作流',
          dataIndex: 'flowId',
        },
        {
          title: '时间ID',
          dataIndex: 'timeId',
          customRender:(text, row, index)=>{
            let result="";
            if(row.timeId){
              result=moment(row.timeId).format()
            }
            return result;
          },
        },
        {
          title: '实例状态',
          dataIndex: 'status',
        },
        {
          title: '创建时间',
          dataIndex: 'createTime',
          customRender:(text, row, index)=>{
            let result="";
            if(row.createTime){
              result=moment(row.createTime).format()
            }
            return result;
          },
        },
        {
          title: '修改时间',
          dataIndex: 'modify_time',
          customRender:(text, row, index)=>{
            let result="";
            if(row.modifyTime){
              result=moment(row.modifyTime).format()
            }
            return result;
          },
        },
        {
          title: '执行ID',
          dataIndex: 'execId',
        },
        {
          title: '开始时间',
          dataIndex: 'startTime',
        },
        {
          title: '结束时间',
          dataIndex: 'endTime',
        },
        {
          title: '执行时长',
          dataIndex: 'difftime',
        },
        {
          title: '执行状态',
          dataIndex: 'exeStatus',
        },
        {
          title: '操作',
          dataIndex: 'operation',
          scopedSlots: { customRender: 'operation' },
        },
      ],      


    };
  },
  computed: {

  },
  methods: {

    searchCallBack(res){
        this.tableData=res.data
    },

    onRedo(key) {
      services.redoFlowInstance(
        key,
        res=>{
          if (res.data.code) {
            this.$message.error(res.data.message);
            return;
          }          
        },
        res=>{
          this.$message.error("redo failed");
        }
      )

    },

  },
};
</script>
