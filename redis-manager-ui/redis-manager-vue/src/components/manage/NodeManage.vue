<template>
  <div id="cluster-manage" class="body-wrapper">
    <div class="manage-header-wrapper" v-loading="clusterLoading">
      <div class="title-wrapper">
        <span>{{ cluster.clusterName }}</span>
        <i class="el-icon-sunny health" title="Status" v-if="cluster.clusterState == 'HEALTH'"></i>
        <i class="el-icon-heavy-rain bad" title="Status" v-else></i>
      </div>

      <div class="base-info-operation-wrapper">
        <span class="base-info-item">
          Node:
          <el-tag size="mini">{{ cluster.clusterKnownNodes }}</el-tag>
        </span>
        <span class="base-info-item" v-if="cluster.redisMode != 'sentinel'">
          Master:
          <el-tag size="mini">{{ cluster.clusterSize }}</el-tag>
        </span>
        <span class="base-info-item" v-else>
          Sentinel OK:
          <el-tag size="mini">{{ cluster.sentinelOk }}</el-tag>
        </span>
        <span class="base-info-item" v-if="cluster.redisMode == 'sentinel'">
          Sentinel Masters:
          <el-tag size="mini">{{ cluster.sentinelMasters }}</el-tag>
        </span>
        <span class="base-info-item" v-if="cluster.redisMode == 'sentinel'">
          Master OK:
          <el-tag size="mini">{{ cluster.masterOk }}</el-tag>
        </span>
        <span class="base-info-item">
          Environment:
          <el-tag size="mini" v-if="cluster.installationEnvironment == 0">Docker</el-tag>
          <el-tag size="mini" v-else-if="cluster.installationEnvironment == 1">Machine</el-tag>
          <el-tag size="mini" v-else-if="cluster.installationEnvironment == 3">Humpback</el-tag>
        </span>
        <span class="base-info-item" v-if="cluster.image != null && cluster.image != ''">
          Image:
          <el-tag size="mini">{{ cluster.image }}</el-tag>
        </span>
        <span class="base-info-item">
          Type:
          <el-tag size="mini" v-if="cluster.installationType == 0">Redis Manager</el-tag>
          <el-tag size="mini" v-else>Import</el-tag>
        </span>
      </div>
    </div>
    <div v-if="sentinelMasterList.length > 0" style="margin-buttom: 20px">
      <el-table :data="sentinelMasterList">
        <el-table-column property="name" label="Master Name"></el-table-column>
        <el-table-column property="status" label="Status">
          <template slot-scope="scope">
            <el-tag size="mini" v-if="scope.row.status == 'ok'">{{ scope.row.status }}</el-tag>
            <el-tag size="mini" type="danger" v-else>{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column property="flags" label="Flags">
          <template slot-scope="scope">
            <el-tag size="mini" v-if="scope.row.flags == 'master'">{{ scope.row.flags }}</el-tag>
            <el-tag size="mini" type="danger" v-else>{{ scope.row.flags }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="monitor" label="Monitor" align="center" sortable>
          <template slot-scope="scope">
            <i class="el-icon-success status-icon normal-status" v-if="scope.row.monitor"></i>
            <i class="el-icon-error status-icon normal-bad" v-else></i>
          </template>
        </el-table-column>
        <el-table-column label="Master Node">
          <template slot-scope="scope">{{ scope.row.host }}:{{ scope.row.port }}</template>
        </el-table-column>
        <el-table-column property="lastMasterNode" label="Last Master Node"></el-table-column>
        <el-table-column property="numSlaves" label="Num Slaves"></el-table-column>
        <el-table-column property="sentinels" label="Sentinels"></el-table-column>
        <el-table-column width="220px">
          <template slot-scope="scope">
            <el-dropdown size="mini" split-button type="primary" trigger="click">
              Info
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item
                  @click.native="sentinelMasterInfoVisible = true; sentinelMaster = scope.row"
                >Detail</el-dropdown-item>
                <el-dropdown-item
                  @click.native="sentinelMasterSlavesVisible = true; sentinelMaster = scope.row"
                >Slaves</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
            <el-dropdown size="mini" split-button type="danger" trigger="click">
              Operate
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item @click.native="editSentinelMaster(scope.row)">Edit</el-dropdown-item>
                <el-dropdown-item
                  @click.native="failoverSentinelMasterVisible = true; sentinelMaster = scope.row"
                >Failover</el-dropdown-item>
                <el-dropdown-item
                  @click.native="deleteSentinelMasterVisible = true; sentinelMaster = scope.row"
                >Delete</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="nodes-wrapper" v-loading="nodeListLoading">
      <div class="batch-operation-wrapper">
        <div class="batch-title">Cluster Operation</div>
        <div style="display: flex; justify-content: space-between;">
          <div v-if="cluster.redisMode != 'sentinel'">
            <!-- <el-link :underline="false" icon="el-icon-finished">Memory Purge</el-link>
            <el-divider direction="vertical"></el-divider>
            <el-link :underline="false" icon="el-icon-zoom-out">Forget</el-link>
            <el-divider direction="vertical"></el-divider>
            <el-link :underline="false" icon="el-icon-video-play">Start</el-link>
            <el-divider direction="vertical"></el-divider>
            <el-link :underline="false" icon="el-icon-switch-button">Stop</el-link>
            <el-divider direction="vertical"></el-divider>
            <el-link :underline="false" icon="el-icon-refresh-left">Restart</el-link>
            <el-divider direction="vertical"></el-divider>
            <el-link :underline="false" icon="el-icon-circle-close">Delete</el-link>
            <el-divider direction="vertical"></el-divider>-->
            <el-link
              :underline="false"
              icon="el-icon-edit"
              @click="editConfigVisible = true"
            >Edit Config</el-link>
            <span
              v-if="cluster.redisMode == 'cluster' && !cluster.initialized && cluster.clusterSlotsAssigned == 0"
            >
              <el-divider direction="vertical"></el-divider>
              <el-link
                :underline="false"
                icon="el-icon-coordinate"
                @click="initSlotsVisible = true"
              >Init Slot</el-link>
            </span>
          </div>
          <div></div>
          <div style="display: flex;align-items: center;">
            <el-link
              :underline="false"
              icon="el-icon-plus"
              type="primary"
              @click="importNodeVisible = true"
              v-if="cluster.redisMode != 'sentinel'"
            >Import Node</el-link>
            <el-link
              :underline="false"
              icon="el-icon-plus"
              type="primary"
              @click="sentinelMasterEditVisible = true; isUpdateSentinelMaster = false; sentinelMaster = {}"
              v-else
            >Monitor Master</el-link>
            <el-divider direction="vertical"></el-divider>
            <i class="el-icon-refresh-left refresh" @click="refresh()"></i>
          </div>
        </div>
      </div>
      <div class="table-wrapper">
        <!-- default-expand-all -->
        <el-table
          :data="redisNodeList"
          style="width: 100%; margin-bottom: 10px;"
          row-key="nodeId"
          size="medium"
          :default-sort="{prop: 'slotRange', order: 'ascending'}"
          :row-class-name="tableRowClassName"
          :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
          :default-expand-all="true"
        >
          <!-- @selection-change="handleSelectionChange"
          <el-table-column type="selection" width="55px"></el-table-column>-->
          <!-- status 有多种 -->
          <el-table-column label="Link State" sortable width="150px">
            <template slot-scope="scope">
              {{ scope.row.slot }}
              <el-tag
                size="mini"
                type="success"
                v-if="scope.row.linkState == 'connected'"
              >{{ scope.row.linkState }}</el-tag>
              <el-tag size="small" class="pointer" v-else type="warning">{{ scope.row.linkState }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="Flags" width="90px">
            <template slot-scope="scope">
              <el-tag size="mini" v-if="scope.row.flags == 'master'">
                {{ scope.row.flags }}
                <span
                  v-if="scope.row.replicaNumber > 0"
                >[{{scope.row.replicaNumber }}]</span>
              </el-tag>
              <el-tag size="mini" class="pointer" type="info" v-else>{{ scope.row.flags }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="inCluster" label="In Cluster" align="center" sortable>
            <template slot-scope="scope">
              <i class="el-icon-success status-icon normal-status" v-if="scope.row.inCluster"></i>
              <i class="el-icon-error status-icon normal-bad" v-else></i>
            </template>
          </el-table-column>
          <el-table-column prop="runStatus" label="Run Status" align="center" sortable>
            <template slot-scope="scope">
              <i class="el-icon-success status-icon normal-status" v-if="scope.row.runStatus"></i>
              <i class="el-icon-error status-icon normal-bad" v-else></i>
            </template>
          </el-table-column>
          <el-table-column prop="host" label="Host" sortable></el-table-column>
          <el-table-column prop="port" label="Port" sortable></el-table-column>
          <el-table-column
            prop="slotRange"
            label="Slot Range"
            sortable
            v-if="cluster.redisMode == 'cluster'"
          >
            <template slot-scope="scope">
              {{ scope.row.slotRange }}
              <el-tag size="mini" v-if="scope.row.slotRange != null">{{ scope.row.slotNumber }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="Meta" width="130px;">
            <template slot-scope="scope">
              <el-tag size="mini" class="pointer" @click="getNodeInfo(scope.row)">Info</el-tag>
              <el-tag
                size="mini"
                class="pointer"
                @click="getConfig(scope.row)"
                v-if="cluster.redisMode != 'sentinel'"
              >Config</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="time" label="Time" sortable></el-table-column>
          <el-table-column label="Operation" width="230px" v-if="cluster.redisMode != 'sentinel'">
            <template slot-scope="scope">
              <el-dropdown size="mini" split-button type="warning" trigger="click">
                Cluster
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item
                    @click.native="handleMoveSlot(scope.row)"
                    v-if="cluster.redisMode == 'cluster' && (scope.row.nodeRole == 'MASTER' && scope.row.inCluster)"
                  >Move Slot</el-dropdown-item>
                  <el-dropdown-item
                    @click.native="handleForget(scope.row)"
                    v-if="scope.row.nodeRole == 'SLAVE' || scope.row.nodeRole == 'REPLICA' || (scope.row.nodeRole == 'MASTER' && (scope.row.children == null || scope.row.children.length == 0))"
                  >Forget</el-dropdown-item>
                  <el-dropdown-item
                    @click.native="handleReplicateOf(scope.row)"
                    v-if="(cluster.redisMode == 'cluster' && scope.row.inCluster && (scope.row.nodeRole == 'SLAVE' || scope.row.nodeRole == 'REPLICA' || (scope.row.nodeRole == 'MASTER' && scope.row.slotRange == null))
                     || (cluster.redisMode == 'standalone' && !scope.row.inCluster))"
                  >Replicate Of</el-dropdown-item>
                  <el-dropdown-item
                    @click.native="handleFailOver(scope.row)"
                    v-if="cluster.redisMode == 'cluster' && (scope.row.nodeRole == 'SLAVE' || scope.row.nodeRole == 'REPLICA') && scope.row.inCluster "
                  >Fail Over</el-dropdown-item>
                  <el-dropdown-item>Memory Purge</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
              <!-- v-if="scope.row.slotRange == null" -->
              <el-dropdown size="mini" split-button type="danger" trigger="click">
                Node
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item
                    @click.native="handleStart(scope.row)"
                    :disabled="scope.row.runStatus"
                  >Start</el-dropdown-item>
                  <el-dropdown-item
                    @click.native="handleStop(scope.row)"
                    :disabled="(scope.row.inCluster || !scope.row.runStatus) && nodeNumber > 1"
                  >Stop</el-dropdown-item>
                  <el-dropdown-item
                    @click.native="handleRestart(scope.row)"
                    :disabled="scope.row.inCluster"
                  >Restart</el-dropdown-item>
                  <el-dropdown-item
                    @click.native="handleDelete(scope.row)"
                    :disabled="scope.row.runStatus"
                  >Delete</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog
      title="Master Node List"
      :visible.sync="replicateOfVisible"
      :close-on-click-modal="false"
      width="80%"
      v-if="replicateOfVisible"
    >
      <el-table
        :data="redisNodeList.filter(redisNode => redisNode.inCluster)"
        stripe
        size="medium"
        :default-sort="{prop: 'slotRange', order: 'ascending'}"
      >
        <!-- status 有多种 -->
        <el-table-column label="Link State" sortable width="150px">
          <template slot-scope="scope">
            {{ scope.row.slot }}
            <el-tag
              size="mini"
              type="success"
              v-if="scope.row.linkState == 'connected'"
            >{{ scope.row.linkState }}</el-tag>
            <el-tag size="small" class="pointer" v-else type="warning">{{ scope.row.linkState }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Flags" width="90px">
          <template slot-scope="scope">
            <el-tag size="mini">{{ scope.row.flags }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Replica" width="80px">
          <template slot-scope="scope">
            <el-tag size="mini">{{scope.row.replicaNumber }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="In Cluster" align="center" sortable>
          <template slot-scope="scope">
            <i class="el-icon-success status-icon normal-status" v-if="scope.row.inCluster"></i>
            <i class="el-icon-error status-icon normal-bad" v-else></i>
          </template>
        </el-table-column>
        <el-table-column label="Run Status" align="center" sortable>
          <template slot-scope="scope">
            <i class="el-icon-success status-icon normal-status" v-if="scope.row.runStatus"></i>
            <i class="el-icon-error status-icon normal-bad" v-else></i>
          </template>
        </el-table-column>
        <el-table-column prop="host" label="Host" sortable></el-table-column>
        <el-table-column prop="port" label="Port" sortable></el-table-column>
        <el-table-column prop="slotRange" label="Slot Range" sortable></el-table-column>
        <el-table-column prop="time" label="Time" sortable></el-table-column>
        <el-table-column label="Operation" width="120px">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="primary"
              @click="replicateOf(scope.row.nodeId)"
              :disabled="(operationNode.nodeId == scope.row.nodeId || operationNode.masterId == scope.row.nodeId ) && operationNode.inCluster"
            >Replicate Of</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog title="Fail Over" :visible.sync="failOverVisible" width="30%" v-if="failOverVisible">
      <span>{{ operationNode.host }}:{{ operationNode.port }} will become the master</span>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="failOverVisible = false">Cancel</el-button>
        <el-button size="small" type="warning" @click="failOver()">Fail Over</el-button>
      </span>
    </el-dialog>

    <el-dialog
      title="Import New Node"
      :visible.sync="importNodeVisible"
      :close-on-click-modal="false"
      width="30%"
      v-if="importNodeVisible"
    >
      <el-form :model="newRedisNode" ref="newRedisNode" size="small">
        <el-form-item label="Redis Node" prop="address" :rules="rules.redisNode">
          <el-input v-model="newRedisNode.address"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="importNodeVisible = false">Cancel</el-button>
        <el-button size="small" type="primary" @click="importNode('newRedisNode')">Confirm</el-button>
      </div>
    </el-dialog>

    <el-dialog
      title="Edit Config"
      :visible.sync="editConfigVisible"
      :close-on-click-modal="false"
      width="30%"
    >
      <el-form :model="redisConfig" ref="redisConfig" size="small" label-position="top">
        <el-form-item label="Config Key" prop="configKey" :rules="rules.configKey">
          <el-select
            v-model="redisConfig.configKey"
            filterable
            allow-create
            default-first-option
            placeholder="Please select config key"
            @change="getConfigCurrentValue(redisConfig)"
          >
            <el-option
              v-for="configKey in configKeyList"
              :key="configKey"
              :label="configKey"
              :value="configKey"
            ></el-option>
          </el-select>
        </el-form-item>
        <!-- <el-form-item label="Current Value" prop="currentValue">
          <span style="color: #409EFF">5546</span>
        </el-form-item>-->
        <el-form-item label="Config Value" prop="configValue">
          <el-input v-model="redisConfig.configValue"></el-input>
        </el-form-item>
        <p v-for="nodeConfig in nodeConfigList" :key="nodeConfig.redisNode">
          <span style="color: #606266;">{{ nodeConfig.redisNode }}</span>
          <span>
            <b>{{ nodeConfig.configValue }}</b>
          </span>
        </p>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="editConfigVisible = false">Cancel</el-button>
        <el-button size="small" type="primary" @click="editConfig('redisConfig')">Confirm</el-button>
      </div>
    </el-dialog>

    <el-dialog title="Forget Node" :visible.sync="forgetVisible" width="30%" v-if="forgetVisible">
      <span>{{ operationNode.host }}:{{ operationNode.port }} will be forget</span>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="forgetVisible = false">Cancel</el-button>
        <el-button size="small" type="warning" @click="forget()">Forget</el-button>
      </span>
    </el-dialog>

    <el-dialog
      title="Move Slot"
      :visible.sync="moveSlotVisible"
      :close-on-click-modal="false"
      width="30%"
      v-if="moveSlotVisible"
    >
      <el-form :model="slotRange" ref="slotRange" size="small">
        <el-form-item prop="startSlot" label="Start Slot" :rules="rules.startSlot">
          <el-input v-model.number="slotRange.startSlot"></el-input>
        </el-form-item>
        <el-form-item prop="endSlot" label="End Slot" :rules="rules.endSlot">
          <el-input v-model.number="slotRange.endSlot"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="moveSlotVisible = false">Cancel</el-button>
        <el-button size="small" type="primary" @click="moveSlot('slotRange')">Confirm</el-button>
      </div>
    </el-dialog>
    <!-- node operation -->
    <el-dialog
      title="Start Node"
      :visible.sync="startNodeVisible"
      width="30%"
      :close-on-click-modal="false"
    >
      <span>{{ operationNode.host }}:{{ operationNode.port }} will be start</span>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="startNodeVisible = false">Cancel</el-button>
        <el-button size="small" type="primary" @click="startNode()">Start</el-button>
      </span>
    </el-dialog>
    <el-dialog
      title="Stop Node"
      :visible.sync="stopNodeVisible"
      width="30%"
      :close-on-click-modal="false"
    >
      <span>{{ operationNode.host }}:{{ operationNode.port }} will be stop</span>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="stopNodeVisible = false">Cancel</el-button>
        <el-button size="small" type="danger" @click="stopNode()">Stop</el-button>
      </span>
    </el-dialog>
    <el-dialog
      title="Restart Node"
      :visible.sync="restartNodeVisible"
      width="30%"
      :close-on-click-modal="false"
    >
      <span>{{ operationNode.host }}:{{ operationNode.port }} will be restart</span>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="restartNodeVisible = false">Cancel</el-button>
        <el-button size="small" type="primary" @click="restartNode()">Restart</el-button>
      </span>
    </el-dialog>
    <el-dialog
      title="Delete Node"
      :visible.sync="deleteNodeVisible"
      width="30%"
      :close-on-click-modal="false"
    >
      <span>{{ operationNode.host }}:{{ operationNode.port }} will be delete</span>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="deleteNodeVisible = false">Cancel</el-button>
        <el-button size="small" type="danger" @click="deleteNode()">Delete</el-button>
      </span>
    </el-dialog>
    <!-- node operation -->

    <el-dialog
      title="Init Slot"
      :visible.sync="initSlotsVisible"
      width="30%"
      :close-on-click-modal="false"
    >
      <p>
        Cluster Slots All:
        <b>16384</b>
      </p>
      <p>
        Cluster Slots Assigned:
        <b>{{ cluster.clusterSlotsAssigned }}</b>
      </p>
      <p>
        Cluster Slots Ok:
        <b>{{ cluster.clusterSlotsOk }}</b>
      </p>
      <p>
        Cluster Slots Pfail:
        <b>{{ cluster.clusterSlotsPfail }}</b>
      </p>
      <p>
        Cluster Slots Fail:
        <b>{{ cluster.clusterSlotsFail }}</b>
      </p>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" type="primary" @click="initSlots()">Init</el-button>
      </span>
    </el-dialog>

    <el-dialog
      :title="'Info - ' + operationNode.host + ':' + operationNode.port"
      :visible.sync="infoVisible"
      width="50%"
      :close-on-click-modal="false"
      top="5vh"
      v-if="infoVisible"
    >
      <info :redisNode="operationNode"></info>
    </el-dialog>

    <el-dialog
      :title="'Config - ' + operationNode.host + ':' + operationNode.port"
      :visible.sync="configVisible"
      width="50%"
      :close-on-click-modal="false"
      top="5vh"
      v-if="configVisible"
    >
      <config :redisNode="operationNode"></config>
    </el-dialog>
    <el-dialog
      title="Sentinel Master Info"
      :visible.sync="sentinelMasterInfoVisible"
      :close-on-click-modal="false"
      v-if="sentinelMasterInfoVisible"
      width="40%"
    >
      <sentinelMasterInfo :sentinelMaster="sentinelMaster"></sentinelMasterInfo>
    </el-dialog>

    <el-dialog
      title="Sentinel Master Slaves"
      :visible.sync="sentinelMasterSlavesVisible"
      :close-on-click-modal="false"
      v-if="sentinelMasterSlavesVisible"
      width="40%"
    >
      <sentinelMasterSlaves :sentinelMaster="sentinelMaster"></sentinelMasterSlaves>
    </el-dialog>

    <el-dialog
      title="Sentinel Master Edit"
      :visible.sync="sentinelMasterEditVisible"
      :close-on-click-modal="false"
      v-if="sentinelMasterEditVisible"
      width="40%"
    >
      <div>
        <el-form
          :model="sentinelMaster"
          :rules="rules"
          ref="sentinelMaster"
          label-width="200px"
          size="small"
        >
          <el-form-item label="Master Name" prop="name">
            <el-tag size="small" v-if="isUpdateSentinelMaster">{{ sentinelMaster.name }}</el-tag>
            <el-input v-model="sentinelMaster.name" maxlength="30" show-word-limit v-else></el-input>
          </el-form-item>
          <el-form-item label="Redis Node" prop="redisNode">
            <el-tag
              size="small"
              v-if="isUpdateSentinelMaster"
            >{{ sentinelMaster.host }}:{{ sentinelMaster.port }}</el-tag>
            <el-input v-model="sentinelMaster.redisNode" v-else></el-input>
          </el-form-item>
          <el-form-item label="Quorum" prop="quorum">
            <el-input v-model.number="sentinelMaster.quorum"></el-input>
          </el-form-item>
          <el-form-item label="Down After Milliseconds" prop="downAfterMilliseconds">
            <el-input v-model.number="sentinelMaster.downAfterMilliseconds"></el-input>
          </el-form-item>
          <el-form-item label="Parallel Syncs" prop="parallelSyncs">
            <el-input v-model.number="sentinelMaster.parallelSyncs"></el-input>
          </el-form-item>
          <el-form-item label="Failover Timeout" prop="failoverTimeout">
            <el-input v-model.number="sentinelMaster.failoverTimeout"></el-input>
          </el-form-item>
          <el-form-item label="Auth Pass" prop="authPass">
            <el-input v-model="sentinelMaster.authPass"></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button
            size="small"
            type="primary"
            @click="updateSentinelMaster('sentinelMaster')"
            v-if="isUpdateSentinelMaster"
          >Update</el-button>
          <el-button
            size="small"
            type="primary"
            @click="monitorMaster('sentinelMaster')"
            v-else
          >Confirm</el-button>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      title="Delete Sentinel Master"
      :visible.sync="deleteSentinelMasterVisible"
      width="30%"
      :close-on-click-modal="false"
    >
      <span>{{ sentinelMaster.host }}:{{ sentinelMaster.port }} will be not monitored</span>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="deleteSentinelMasterVisible = false">Cancel</el-button>
        <el-button size="small" type="danger" @click="deleteSentinelMaster()">Delete</el-button>
      </span>
    </el-dialog>

    <el-dialog
      title="Failover Sentinel Master"
      :visible.sync="failoverSentinelMasterVisible"
      width="30%"
      :close-on-click-modal="false"
    >
      <span>{{ sentinelMaster.host }}:{{ sentinelMaster.port }} will failover</span>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="failoverSentinelMasterVisible = false">Cancel</el-button>
        <el-button size="small" type="danger" @click="failoverSentinelMaster()">Failover</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import info from "@/components/view/Info";
import config from "@/components/view/Config";
import sentinelMasterInfo from "@/components/view/SentinelMasterInfo";
import sentinelMasterSlaves from "@/components/view/SentinelMasterSlaves";
import { isEmpty, validateIpAndPort } from "@/utils/validate.js";
import { formatTime } from "@/utils/time.js";
import API from "@/api/api.js";
import { store } from "@/vuex/store.js";
import { getClusterById } from "@/components/cluster/cluster.js";
import message from "@/utils/message.js";
export default {
  components: {
    info,
    config,
    sentinelMasterInfo,
    sentinelMasterSlaves
  },
  data() {
    var validateRedisNode = (rule, value, callback) => {
      if (!validateIpAndPort(value)) {
        return callback(new Error("Incorrect format"));
      }
      callback();
    };
    var validateConnection = (rule, value, callback) => {
      let redisPassword =
        this.cluster.redisMode != "sentinel"
          ? this.cluster.redisPassword
          : this.sentinelMaster.authPass;
      let data = {
        redisPassword: redisPassword
      };
      let ipAndPort = value.split(":");
      let redisNode = {
        host: ipAndPort[0],
        port: ipAndPort[1]
      };
      data.redisNode = redisNode;
      let url = "/validate/redisNode";
      API.post(
        url,
        data,
        response => {
          if (response.data.code != 0) {
            return callback(new Error("Connection refused."));
          } else {
            callback();
          }
        },
        err => {
          return callback(new Error("Network error, " + err));
        }
      );
    };
    var validateSlot = (rule, value, callback) => {
      if (!Number.isInteger(value)) {
        callback(new Error("Please enter number"));
      } else if (value < 0 || value > 16383) {
        callback(new Error("Slot range between 0 and 16383"));
      }
      callback();
    };
    var validateEndSlotGreater = (rule, value, callback) => {
      var startSlot = this.slotRange.startSlot;
      if (isEmpty(startSlot)) {
        callback();
      }
      if (value - startSlot < 0) {
        callback(new Error("End slot must be greater than the start slot"));
      }
      callback();
    };
    var validateMasterName = (rule, value, callback) => {
      if (this.isUpdateSentinelMaster) {
        return callback();
      }
      let url = "/sentinel/getSentinelMasterByName";
      let sentinelMaster = {
        clusterId: this.cluster.clusterId,
        groupId: this.cluster.groupId,
        name: value
      };
      API.post(
        url,
        sentinelMaster,
        response => {
          if (response.data.code == 0) {
            return callback(new Error("Master name has been exist."));
          } else {
            callback();
          }
        },
        err => {
          return callback(new Error("Network error, " + err));
        }
      );
    };
    return {
      cluster: {},
      search: "",
      redisNodeList: [],
      replicateOfVisible: false,
      failOverVisible: false,
      importNodeVisible: false,
      editConfigVisible: false,
      forgetVisible: false,
      moveSlotVisible: false,
      startNodeVisible: false,
      stopNodeVisible: false,
      restartNodeVisible: false,
      deleteNodeVisible: false,
      infoVisible: false,
      configVisible: false,
      initSlotsVisible: false,
      slotRange: {},
      operationNode: {},
      operationNodeList: [],
      newRedisNode: {},
      configKeyList: [],
      redisConfig: {},
      nodeConfigList: [],
      rules: {
        redisNode: [
          {
            required: true,
            message: "Please enter redis node",
            trigger: "blur"
          },
          { required: true, validator: validateRedisNode, trigger: "blur" },
          { required: true, validator: validateConnection, trigger: "blur" }
        ],
        startSlot: [
          {
            required: true,
            message: "Please enter slot",
            trigger: "blur"
          },
          { required: true, validator: validateSlot, trigger: "blur" }
        ],
        endSlot: [
          {
            required: true,
            message: "Please enter slot",
            trigger: "blur"
          },
          { required: true, validator: validateSlot, trigger: "blur" },
          { required: true, validator: validateEndSlotGreater, trigger: "blur" }
        ],
        configKey: [
          {
            required: true,
            message: "Please select or enter config key",
            trigger: "blur"
          }
        ],
        name: [
          {
            required: true,
            message: "Please enter master name",
            trigger: "blur"
          },
          { required: true, validator: validateMasterName, trigger: "blur" }
        ],
        downAfterMilliseconds: [
          {
            type: "number",
            message: "Please enter integer",
            trigger: "blur"
          }
        ],
        parallelSyncs: [
          {
            type: "number",
            message: "Please enter integer",
            trigger: "blur"
          }
        ],
        failoverTimeout: [
          {
            type: "number",
            message: "Please enter integer",
            trigger: "blur"
          }
        ],
        quorum: [
          {
            required: true,
            message: "Please enter quorum",
            trigger: "blur"
          },
          {
            type: "number",
            message: "Please enter integer",
            trigger: "blur"
          }
        ]
      },
      clusterLoading: false,
      nodeListLoading: false,
      operationLoading: false,
      sentinelMasterList: [],
      sentinelMaster: {},
      sentinelMasterInfoVisible: false,
      sentinelMasterEditVisible: false,
      isUpdateSentinelMaster: false,
      deleteSentinelMasterVisible: false,
      failoverSentinelMasterVisible: false,
      sentinelMasterSlavesVisible: false
    };
  },
  methods: {
    tableRowClassName({ row, rowIndex }) {
      if (row.nodeRole == "MASTER") {
        return "info-row";
      }
      return "";
    },
    handleSelectionChange(redisNodeList) {
      this.operationNode = [];
      redisNodeList.forEach(redisNode => {
        this.operationNode.push(redisNode);
      });
    },
    getAllNodeList(clusterId) {
      this.nodeListLoading = true;
      let url = "/node-manage/getAllNodeListWithStatus/" + clusterId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          // console.log(result.data);
          if (result.code == 0) {
            let nodeList = result.data;
            let redisNodeList = [];
            let masterRedisNode;
            let children = [];
            nodeList.forEach(node => {
              let nodeId = node.nodeId;
              if (isEmpty(nodeId)) {
                node.nodeId = node.host + ":" + node.port;
              }
              let flags = node.flags;
              node.time = formatTime(node.updateTime);
              if (flags == "master") {
                children = [];
                masterRedisNode = node;
                masterRedisNode.children = children;
                redisNodeList.push(masterRedisNode);
              } else if (flags == "slave" || flags == "replica") {
                if (node.runStatus) {
                  children.push(node);
                } else {
                  node.children = [];
                  redisNodeList.push(node);
                }
              } else {
                redisNodeList.push(node);
              }
            });
            redisNodeList.forEach(redisNode => {
              let children = redisNode.children;
              let replicaNumber = 0;
              if (!isEmpty(children) && !isEmpty(children.length)) {
                replicaNumber = children.length;
              }
              redisNode.replicaNumber = replicaNumber;
            });
            this.redisNodeList = redisNodeList;
          } else {
            message.error(result.message);
          }
          this.nodeListLoading = false;
        },
        err => {
          this.nodeListLoading = false;
          message.error(err);
        }
      );
    },
    reload() {
      let clusterId = this.cluster.clusterId;
      this.getAllNodeList(clusterId);
      getClusterById(clusterId, cluster => {
        this.cluster = cluster;
        this.getAllNodeList(clusterId);
        this.getConfigKeyList();
      });
      this.updateCluster(this.clsuter);
    },
    updateCluster(cluster) {
      // axios
      let url = "/cluster/updateCluster";
      API.post(
        url,
        this.cluster,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.cluster = result.data;
          } else {
            message.error(result.message);
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    getNodeInfo(redisNode) {
      this.operationNode = redisNode;
      this.infoVisible = true;
    },
    getConfig(redisNode) {
      this.operationNode = redisNode;
      this.configVisible = true;
    },
    getConfigKeyList() {
      let url = "/node-manage/getRedisConfigKeyList";
      API.get(
        url,
        null,
        response => {
          this.configKeyList = response.data.data;
        },
        err => {
          message.error(err);
        }
      );
    },
    getConfigCurrentValue(redisConfig) {
      let url = "/node-manage/getConfigCurrentValue";
      let data = {
        cluster: this.cluster,
        configKey: redisConfig.configKey
      };
      API.post(
        url,
        data,
        response => {
          this.nodeConfigList = response.data.data;
        },
        err => {
          message.error(err);
        }
      );
    },
    editConfig(redisConfig) {
      this.$refs[redisConfig].validate(valid => {
        if (valid) {
          let url = "/node-manage/updateRedisConfig";
          let data = {
            clusterId: this.cluster.clusterId,
            redisConfig: this.redisConfig,
            groupId: this.currentGroup.groupId
          };
          API.post(
            url,
            data,
            response => {
              let result = response.data;
              if (result.code == 0) {
                this.editConfigVisible = false;
              } else {
                message.error("update config failed");
              }
            },
            err => {
              message.error(err);
            }
          );
        }
      });
    },
    canOperate() {
      if (this.nodeNumber <= 1) {
        return true;
      }
      let nodes = this.cluster.nodes;
      let nodeArr = nodes.split(",");
      this.operationNodeList.forEach(redisNode => {
        let node = redisNode.host + ":" + redisNode.port;
        if (nodes.indexOf(node) > -1 && nodeArr.length == 1) {
          message.warning(
            "I can't operate " + node + ", because it in the database"
          );
          return false;
        }
      });
      return true;
    },
    buildNodeList(data) {
      let isArray = !isEmpty(data.length);
      this.operationNodeList = [];
      if (isArray) {
      } else {
        this.operationNode = {};
        this.operationNode = data;
        this.operationNode.groupId = this.currentGroup.groupId;
        this.operationNodeList.push(this.operationNode);
      }
    },
    handleReplicateOf(redisNode) {
      this.replicateOfVisible = true;
      this.buildNodeList(redisNode);
    },
    replicateOf(nodeId) {
      if (isEmpty(nodeId)) {
        message.error("Node invalid");
        return;
      }
      this.operationNodeList.forEach(redisNode => {
        redisNode.masterId = nodeId;
      });
      let redisMode = this.cluster.redisMode;
      let url;
      if (redisMode == "cluster") {
        url = "/node-manage/replicateOf";
      } else if (redisMode == "standalone") {
        url = "/node-manage/standaloneReplicateOf";
      } else {
        return;
      }
      API.post(
        url,
        this.operationNodeList,
        response => {
          let result = response.data;
          this.reload();
          if (result.code == 0) {
            this.replicateOfVisible = false;
          } else {
            message.error(result.message);
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    handleFailOver(redisNode) {
      this.failOverVisible = true;
      this.buildNodeList(redisNode);
    },
    failOver() {
      let url = "/node-manage/failOver";
      API.post(
        url,
        this.operationNodeList,
        response => {
          let result = response.data;
          this.getAllNodeList(this.cluster.clusterId);
          if (result.code == 0) {
            this.failOverVisible = false;
          } else {
            message.error(result.message);
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    importNode(newRedisNode) {
      this.$refs[newRedisNode].validate(valid => {
        if (valid) {
          let ipAndPort = this.newRedisNode.address.split(":");
          let redisNode = {
            clusterId: this.cluster.clusterId,
            host: ipAndPort[0],
            port: ipAndPort[1],
            groupId: this.currentGroup.groupId
          };
          let redisNodeList = [];
          redisNodeList.push(redisNode);
          let url = "/node-manage/importNode";
          API.post(
            url,
            redisNodeList,
            response => {
              let result = response.data;
              this.reload();
              if (result.code == 0) {
                this.importNodeVisible = false;
              } else {
                message.error(result.message);
              }
            },
            err => {
              message.error(err);
            }
          );
        }
      });
    },
    handleForget(redisNode) {
      this.forgetVisible = true;
      this.buildNodeList(redisNode);
    },
    forget() {
      let redisMode = this.cluster.redisMode;
      let url;
      if (redisMode == "cluster") {
        url = "/node-manage/forget";
      } else if (redisMode == "standalone") {
        url = "/node-manage/standaloneForget";
      } else {
        return;
      }
      if (!this.canOperate()) {
        return;
      }
      API.post(
        url,
        this.operationNodeList,
        response => {
          let result = response.data;
          this.reload();
          if (result.code == 0) {
            this.forgetVisible = false;
          } else {
            message.error(result.message);
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    handleMoveSlot(redisNode) {
      this.moveSlotVisible = true;
      this.buildNodeList(redisNode);
    },
    moveSlot(slotRange) {
      this.$refs[slotRange].validate(valid => {
        if (valid) {
          let data = {
            redisNode: this.operationNode,
            slotRange: this.slotRange,
            groupId: this.currentGroup.groupId
          };
          let url = "/node-manage/moveSlot";
          API.post(
            url,
            data,
            response => {
              let result = response.data;
              this.getAllNodeList(this.cluster.clusterId);
              if (result.code == 0) {
                this.moveSlotVisible = false;
                this.$refs[slotRange].resetFields();
              } else {
                message.error(result.message + " Move slot failed");
              }
            },
            err => {
              message.error(err);
            }
          );
        }
      });
    },
    handleStart(redisNode) {
      let runStatus = redisNode.runStatus;
      if (runStatus) {
        message.error("This node is already running");
        return;
      }
      this.buildNodeList(redisNode);
      this.startNodeVisible = true;
    },
    startNode() {
      let url = "/node-manage/start";
      API.post(
        url,
        this.operationNodeList,
        response => {
          let result = response.data;
          this.reload();
          if (result.code == 0) {
            this.startNodeVisible = false;
          } else {
            message.error("start node failed");
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    handleStop(redisNode) {
      let inCluster = redisNode.inCluster;
      let runStatus = redisNode.runStatus;
      if (!runStatus) {
        message.error("This node has stopped");
        return;
      }
      this.buildNodeList(redisNode);
      this.stopNodeVisible = true;
    },
    stopNode() {
      let url = "/node-manage/stop";
      if (!this.canOperate) {
        return;
      }
      API.post(
        url,
        this.operationNodeList,
        response => {
          let result = response.data;
          this.reload();
          if (result.code == 0) {
            this.stopNodeVisible = false;
          } else {
            message.error("stop node failed");
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    handleRestart(redisNode) {
      this.buildNodeList(redisNode);
      this.restartNodeVisible = true;
    },
    restartNode() {
      let url = "/node-manage/restart";
      if (!this.canOperate) {
        return;
      }
      API.post(
        url,
        this.operationNodeList,
        response => {
          let result = response.data;
          this.reload();
          if (result.code == 0) {
            this.restartNodeVisible = false;
          } else {
            message.error("restart node failed");
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    handleDelete(redisNode) {
      let inCluster = redisNode.inCluster;
      let runStatus = redisNode.runStatus;
      if (inCluster) {
        message.error(
          redisNode.host +
            ":" +
            redisNode.port +
            " still in the cluster, please forget it first"
        );
        return;
      } else if (runStatus) {
        message.error(
          redisNode.host +
            ":" +
            redisNode.port +
            " is running, please stop it first"
        );
        return;
      }
      this.buildNodeList(redisNode);
      this.deleteNodeVisible = true;
    },
    deleteNode() {
      let url = "/node-manage/delete";
      if (!this.canOperate) {
        return;
      }
      API.post(
        url,
        this.operationNodeList,
        response => {
          let result = response.data;
          this.reload();
          if (result.code == 0) {
            this.deleteNodeVisible = false;
          } else {
            message.error("delete node failed");
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    initSlots() {
      let url = "/node-manage/initSlots";
      API.post(
        url,
        this.cluster,
        response => {
          let result = response.data;
          this.reload();
          if (result.code == 0) {
            this.initSlotsVisible = false;
          } else {
            message.error(result.message);
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    refresh() {
      let clusterId = this.cluster.clusterId;
      getClusterById(clusterId, cluster => {
        this.cluster = cluster;
        this.getAllNodeList(clusterId);
      });
    },
    getSentinelMasterList(clusterId) {
      let url = "/sentinel/getSentinelMasterList/" + clusterId;
      API.get(
        url,
        null,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.sentinelMasterList = result.data;
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    monitorMaster(sentinelMaster) {
      this.$refs[sentinelMaster].validate(valid => {
        if (valid) {
          let hostAndPort = this.sentinelMaster.redisNode.split(":");
          this.sentinelMaster.host = hostAndPort[0];
          this.sentinelMaster.port = hostAndPort[1];
          this.sentinelMaster.clusterId = this.cluster.clusterId;
          this.sentinelMaster.groupId = this.cluster.groupId;
          let url = "/sentinel/monitorMaster";
          API.post(
            url,
            this.sentinelMaster,
            response => {
              if (response.data.code == 0) {
                this.sentinelMasterEditVisible = false;
              } else {
                message.error("Monitor new master failed.");
              }
              this.getSentinelMasterList(this.cluster.clusterId);
            },
            err => {
              message.error(err);
            }
          );
        }
      });
    },
    updateSentinelMaster(sentinelMaster) {
      this.$refs[sentinelMaster].validate(valid => {
        if (valid) {
          let url = "/sentinel/updateSentinelMaster";
          API.post(
            url,
            this.sentinelMaster,
            response => {
              if (response.data.code == 0) {
                this.sentinelMasterEditVisible = false;
              } else {
                message.error("Update sentinel master failed.");
              }
              this.getSentinelMasterList(this.cluster.clusterId);
            },
            err => {
              message.error(err);
            }
          );
        }
      });
    },
    editSentinelMaster(sentinelMaster) {
      this.isUpdateSentinelMaster = true;
      this.sentinelMasterEditVisible = true;
      let url = "/sentinel/getSentinelMasterByName";
      API.post(
        url,
        sentinelMaster,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.sentinelMaster = result.data;
            this.sentinelMaster.redisNode =
              this.sentinelMaster.host + ":" + this.sentinelMaster.port;
          } else {
            message.error("Get sentinel master failed.");
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    deleteSentinelMaster() {
      let url = "/sentinel/deleteSentinelMaster";
      API.post(
        url,
        this.sentinelMaster,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.deleteSentinelMasterVisible = false;
            this.getSentinelMasterList(this.cluster.clusterId);
          } else {
            message.error("Remove sentinel master failed.");
          }
        },
        err => {
          message.error(err);
        }
      );
    },
    failoverSentinelMaster() {
      let url = "/sentinel/failoverSentinelMaster";
      API.post(
        url,
        this.sentinelMaster,
        response => {
          let result = response.data;
          if (result.code == 0) {
            this.deleteSentinelMasterVisible = false;
            this.getSentinelMasterList(this.cluster.clusterId);
          } else {
            message.error("Failover sentinel master failed.");
          }
        },
        err => {
          message.error(err);
        }
      );
    }
  },
  computed: {
    nodeNumber() {
      let number = 0;
      this.redisNodeList.forEach(masterNode => {
        number += 1;
        let children = masterNode.children;
        if (!isEmpty(children) && !isEmpty(children.length)) {
          number += children.length;
        }
      });
      return number;
    },
    // 监听group变化
    currentGroup() {
      return store.getters.getCurrentGroup;
    }
  },
  watch: {
    currentGroup(group) {
      this.$router.push({
        name: "dashboard",
        params: { groupId: group.groupId }
      });
    }
  },
  mounted() {
    let clusterId = this.$route.params.clusterId;
    getClusterById(clusterId, cluster => {
      this.cluster = cluster;
      this.getAllNodeList(clusterId);
      this.getConfigKeyList();
      if (cluster.redisMode == "sentinel") {
        this.getSentinelMasterList(clusterId);
      }
    });
  }
};
</script>

<style scoped>
#cluster-manage {
  min-width: 1000px;
}

.manage-header-wrapper {
  padding-bottom: 20px;
  border-bottom: 1px solid #dcdfe6;
}

.base-info-operation-wrapper {
  margin-top: 20px;
  display: flex;
  align-items: center;
}

.bad {
  color: #f4516c;
}

.base-info-item {
  margin-right: 20px;
}

.batch-title {
  margin-bottom: 10px;
  color: #909399;
  font-size: 14px;
}

.nodes-wrapper,
.table-wrapper {
  margin-top: 20px;
}

.el-table >>> .info-row {
  background: #fafafa !important;
}

.master-line {
  background-color: #f5f7fa;
}

.status-icon {
  font-size: 16px;
}

.normal-status {
  color: #40c9c6;
}

.bad-status {
  color: #f4516c;
}

.refresh {
  font-size: 20px;
  cursor: pointer;
  color: #909399;
}

.refresh:hover {
  color: #2c3e50;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>