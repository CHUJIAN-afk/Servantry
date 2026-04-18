package first.servantry.api.ai;

public enum NodeStatus {
    SUCCESS, // 节点执行成功（例如：移动到了目的地，或者条件判断为真）
    FAILURE, // 节点执行失败（例如：目标丢失，或者条件判断为假）
    RUNNING  // 节点正在执行中（例如：正在寻路中，需要下一 tick 继续）
}