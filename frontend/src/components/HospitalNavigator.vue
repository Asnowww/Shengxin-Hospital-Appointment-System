<template>
  <transition name="nav-fade">
    <div v-if="visible" class="nav-overlay">
      <div class="nav-shell">
        <header class="nav-header">
          <div class="nav-title">
            <svg xmlns="http://www.w3.org/2000/svg" class="title-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 9l9-7 9 7" />
              <path d="M9 22V12h6v10" />
              <path d="M21 22H3" />
            </svg>
            <div>
              <p class="eyebrow">圣心医院</p>
              <p class="headline">楼层 3D 导航</p>
            </div>
          </div>
          <button class="close-btn" @click="handleClose" aria-label="关闭导航">×</button>
        </header>

        <div class="nav-body">
          <div ref="mountRef" class="nav-canvas"></div>

          <div class="nav-panel">
            <div class="panel-section">
              <p class="section-title">当前位置</p>
              <div class="search-box">
                <div class="input-shell accent" @click.stop="openStartDropdown">
                  <svg xmlns="http://www.w3.org/2000/svg" class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 10c0 7-9 12-9 12S3 17 3 10a9 9 0 0 1 18 0" />
                    <circle cx="12" cy="10" r="3" />
                  </svg>
                  <input
                    v-model="startSearch"
                    type="text"
                    class="input-field"
                    placeholder="选择/搜索当前位置"
                    aria-label="起点"
                    @focus="openStartDropdown"
                    @input="handleStartSearch"
                    @keydown.enter.prevent="selectFirstStartOption"
                    @blur="delayedCloseStartDropdown"
                  />
                </div>
                <div v-if="startDropdownOpen" class="dropdown-panel">
                  <div class="dropdown-scroll">
                    <button
                      v-for="option in filteredStartOptions"
                      :key="option.value"
                      type="button"
                      class="dropdown-option"
                      @click="selectStartOption(option)"
                    >
                      <span class="option-label">{{ option.label }}</span>
                      <span class="option-meta">{{ option.dept || option.desc || '' }}</span>
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <div class="panel-section">
              <p class="section-title">前往科室</p>
              <div class="search-box">
                <div class="input-shell accent" @click.stop="openEndDropdown">
                  <svg xmlns="http://www.w3.org/2000/svg" class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="11" cy="11" r="8" />
                    <path d="m21 21-4.35-4.35" />
                  </svg>
                  <input
                    v-model="endSearch"
                    type="text"
                    class="input-field"
                    placeholder="选择/搜索目的地"
                    aria-label="选择目的地"
                    @focus="openEndDropdown"
                    @input="handleEndSearch"
                    @keydown.enter.prevent="selectFirstEndOption"
                    @blur="delayedCloseEndDropdown"
                  />
                </div>
                <div v-if="endDropdownOpen" class="dropdown-panel">
                  <div class="dropdown-scroll">
                    <button
                      v-for="dest in filteredEndOptions"
                      :key="dest.value"
                      type="button"
                      class="dropdown-option"
                      @click="selectEndOption(dest)"
                    >
                      <span class="option-label">{{ dest.label }}</span>
                      <span class="option-meta">{{ dest.dept || dest.desc || '' }}</span>
                    </button>
                  </div>
                </div>
              </div>
              <p v-if="appointmentInfoText" class="hint-line">
                本次预约：{{ appointmentInfoText }}
              </p>
            </div>

            <button
              class="nav-action"
              :disabled="!endPoint"
              @click="handleNavigate"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="action-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 19V5" />
                <path d="M7 12l5-5 5 5" />
              </svg>
              {{ isNavigating ? '重新规划' : '开始导航' }}
            </button>

            <div class="legend">
              <p class="legend-title">楼层分布</p>
              <div class="legend-grid">
                <div class="legend-item"><span class="dot green"></span>1F 老年病科 · 风湿免疫科 · 高血压科</div>
                <div class="legend-item"><span class="dot green"></span>2F 呼吸内科 · 内分泌科 · 神经内科</div>
                <div class="legend-item"><span class="dot blue"></span>3F 神经内科(高区) · 骨科 · 功能神外 · 心脏外科</div>
                <div class="legend-item"><span class="dot blue"></span>4F 心脏外科 · 神经外科 · 烧伤整形</div>
                <div class="legend-item"><span class="dot orange"></span>5F 烧伤整形 · 普胸外科 · 小儿内外科</div>
                <div class="legend-item"><span class="dot pink"></span>6F 妇科 · 核医学科 · 小儿外科</div>
              </div>
            </div>
          </div>

          <div class="nav-hint">
            按住左键旋转 · 右键平移 · 滚轮缩放
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch, shallowReactive } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { CSS2DRenderer, CSS2DObject } from 'three/examples/jsm/renderers/CSS2DRenderer.js'

const FLOOR_HEIGHT = 20
const FLOOR_WIDTH = 60
const FLOOR_LENGTH = 40
const ELEVATOR_POS = { x: 0, z: -15 }
const ENTRANCE_POS = { x: 0, z: 25 }
const BUILDING_NAME = '圣心楼'

const GRID_LANES = [-14, -6, 6, 14]
const GRID_START_X = -24
const GRID_STEP_X = 8
const ROOM_SIZE = { w: 6.2, l: 6.2 }

const FLOOR_META = {
  1: { name: '一层 · 内科门诊', description: '老年病科 / 风湿免疫科 / 高血压科' },
  2: { name: '二层 · 专科内科', description: '呼吸内科 / 内分泌科 / 神经内科 / 高血压科' },
  3: { name: '三层 · 神经与骨科外科', description: '神经内科(高区) / 骨科 / 功能神经外科 / 心脏外科' },
  4: { name: '四层 · 神外与烧伤中心', description: '心脏外科 / 神经外科 / 烧伤整形科' },
  5: { name: '五层 · 胸外与儿科', description: '烧伤整形科 / 普胸外科 / 小儿内外科' },
  6: { name: '六层 · 妇科与医技', description: '妇科 / 核医学科 / 小儿外科' }
}

const DEPT_CATEGORY_MAP = {
  老年病科: '内科',
  风湿免疫科: '内科',
  高血压科: '内科',
  呼吸内科: '内科',
  内分泌科: '内科',
  神经内科: '内科',
  骨科: '外科',
  功能神经外科: '外科',
  心脏外科: '外科',
  神经外科: '外科',
  烧伤整形科: '外科',
  普胸外科: '外科',
  小儿内科: '儿科',
  小儿外科: '儿科',
  妇科: '妇产科',
  核医学科: '医技'
}

const makeRange = (start, end, buildLabel) => {
  const length = end - start + 1
  return Array.from({ length }, (_, idx) => {
    const roomNumber = start + idx
    return {
      id: String(roomNumber),
      label: buildLabel ? buildLabel(idx, roomNumber) : `${roomNumber} 诊室`
    }
  })
}

const buildDeptRooms = (floor, dept, items) =>
  items.map(item => ({
    floor,
    id: item.id,
    name: item.label,
    dept,
    building: BUILDING_NAME,
    category: DEPT_CATEGORY_MAP[dept] || '内科',
    desc: item.desc || item.label
  }))

const applyGridLayout = rooms =>
  rooms.map((room, index) => {
    const laneIndex = index % GRID_LANES.length
    const colIndex = Math.floor(index / GRID_LANES.length)
    return {
      ...ROOM_SIZE,
      ...room,
      x: GRID_START_X + colIndex * GRID_STEP_X,
      z: GRID_LANES[laneIndex]
    }
  })

const RAW_ROOMS = [
  ...buildDeptRooms(1, '老年病科', [
    { id: '101', label: '老年病科办公室' },
    ...makeRange(102, 108, idx => `老年病科诊室${idx + 1}`)
  ]),
  ...buildDeptRooms(1, '风湿免疫科', [
    { id: '109', label: '风湿免疫科办公室' },
    ...makeRange(110, 116, idx => `风湿免疫科诊室${idx + 1}`)
  ]),
  ...buildDeptRooms(1, '高血压科', [
    { id: '117', label: '高血压科办公室' },
    ...makeRange(118, 122, idx => `高血压科诊室${idx + 1}`)
  ]),
  ...buildDeptRooms(2, '高血压科', [
    ...makeRange(201, 202, idx => `高血压科诊室${idx + 6}`)
  ]),
  ...buildDeptRooms(2, '呼吸内科', [
    { id: '203', label: '呼吸内科办公室' },
    ...makeRange(204, 210, idx => `呼吸内科诊室${idx + 1}`)
  ]),
  ...buildDeptRooms(2, '内分泌科', [
    { id: '211', label: '内分泌科办公室' },
    ...makeRange(212, 218, idx => `内分泌科诊室${idx + 1}`)
  ]),
  ...buildDeptRooms(2, '神经内科', [
    { id: '219', label: '神经内科办公室' },
    ...makeRange(220, 222, idx => `神经内科诊室${idx + 1}`)
  ]),
  ...buildDeptRooms(3, '神经内科', [
    ...makeRange(301, 304, idx => `神经内科诊室${idx + 4}`)
  ]),
  ...buildDeptRooms(3, '骨科', [
    { id: '305', label: '骨科办公室' },
    ...makeRange(306, 312, idx => `骨科诊室${idx + 1}`)
  ]),
  ...buildDeptRooms(3, '功能神经外科', [
    { id: '313', label: '功能神经外科办公室' },
    ...makeRange(314, 320, idx => `功能神经外科诊室${idx + 1}`)
  ]),
  ...buildDeptRooms(3, '心脏外科', [
    { id: '321', label: '心脏外科办公室' },
    { id: '322', label: '心脏外科诊室1' }
  ]),
  ...buildDeptRooms(4, '心脏外科', [
    ...makeRange(401, 407, idx => `心脏外科诊室${idx + 2}`)
  ]),
  ...buildDeptRooms(4, '神经外科', [
    { id: '408', label: '神经外科办公室' },
    ...makeRange(409, 415, idx => `神经外科诊室${idx + 1}`)
  ]),
  ...buildDeptRooms(4, '烧伤整形科', [
    { id: '416', label: '烧伤整形科办公室' },
    ...makeRange(417, 422, idx => `烧伤整形科诊室${idx + 1}`)
  ]),
  ...buildDeptRooms(5, '烧伤整形科', [
    { id: '501', label: '烧伤整形科诊室7' }
  ]),
  ...buildDeptRooms(5, '普胸外科', [
    { id: '502', label: '普胸外科办公室' },
    ...makeRange(503, 509, idx => `普胸外科诊室${idx + 1}`)
  ]),
  ...buildDeptRooms(5, '小儿内科', [
    { id: '510', label: '小儿内科办公室' },
    ...makeRange(511, 517, idx => `小儿内科诊室${idx + 1}`)
  ]),
  ...buildDeptRooms(5, '小儿外科', [
    { id: '518', label: '小儿外科办公室' },
    ...makeRange(519, 522, idx => `小儿外科诊室${idx + 1}`)
  ]),
  ...buildDeptRooms(6, '小儿外科', [
    ...makeRange(601, 603, idx => `小儿外科诊室${idx + 5}`)
  ]),
  ...buildDeptRooms(6, '妇科', [
    { id: '604', label: '妇科办公室' },
    ...makeRange(605, 612, idx => `妇科诊室${idx + 1}`)
  ]),
  ...buildDeptRooms(6, '核医学科', [
    { id: '613', label: '核医学科办公室' },
    ...makeRange(614, 620, idx => `核医学科诊室${idx + 1}`)
  ])
]

const buildHospitalData = () => {
  const grouped = RAW_ROOMS.reduce((acc, room) => {
    acc[room.floor] = acc[room.floor] || []
    acc[room.floor].push(room)
    return acc
  }, {})

  return Object.entries(grouped)
    .sort(([a], [b]) => Number(a) - Number(b))
    .map(([floor, rooms]) => {
      const meta = FLOOR_META[floor] || {}
      return {
        floor: Number(floor),
        name: meta.name || `${floor}F`,
        description: meta.description || '',
        rooms: applyGridLayout(rooms)
      }
    })
}

const HOSPITAL_DATA = buildHospitalData()

const props = defineProps({
  visible: { type: Boolean, default: false },
  defaultDestination: { type: String, default: '' },
  appointmentInfo: { type: Object, default: () => ({}) }
})
const emit = defineEmits(['close'])

const mountRef = ref(null)
const sceneState = shallowReactive({
  scene: null,
  camera: null,
  renderer: null,
  labelRenderer: null,
  controls: null
})
const initialized = ref(false)

const startPoint = ref('entrance')
const endPoint = ref(props.defaultDestination || '')
const isNavigating = ref(false)
const startSearch = ref('')
const endSearch = ref('')
const startDropdownOpen = ref(false)
const endDropdownOpen = ref(false)

const animationState = reactive({
  curve: null,
  progress: 0,
  speed: 0.002
})

const pathMesh = ref(null)
const avatar = ref(null)
const rafId = ref(null)
let initTries = 0

const destinations = computed(() => {
  const list = []
  HOSPITAL_DATA.forEach(floor => {
    floor.rooms.forEach(room => {
      list.push({
        ...room,
        floor: floor.floor,
        value: room.id,
        label: `${floor.floor}F - ${room.name} (${room.id})`
      })
    })
  })
  return list
})

const findOptionByValue = (value, list) => list.find(item => item.value === value)

const locationOptions = computed(() => [
  {
    value: 'entrance',
    label: '医院大门（默认）',
    floor: 1,
    x: ENTRANCE_POS.x,
    z: ENTRANCE_POS.z,
    desc: '入口'
  },
  ...destinations.value
])

const currentStartLabel = computed(() => findOptionByValue(startPoint.value, locationOptions.value)?.label || '')
const currentEndLabel = computed(() => findOptionByValue(endPoint.value, destinations.value)?.label || '')

const filterOptions = (options, keyword) => {
  const key = (keyword || '').trim().toLowerCase()
  if (!key) return options
  return options.filter(opt => {
    const combined = [opt.label, opt.name, opt.dept, opt.desc, opt.id]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
    return combined.includes(key)
  })
}

const filteredStartOptions = computed(() => filterOptions(locationOptions.value, startSearch.value))
const filteredEndOptions = computed(() => filterOptions(destinations.value, endSearch.value))

const syncStartSearchLabel = () => {
  startSearch.value = currentStartLabel.value || locationOptions.value[0]?.label || ''
}
const syncEndSearchLabel = () => {
  endSearch.value = currentEndLabel.value || ''
}

syncStartSearchLabel()
syncEndSearchLabel()

const appointmentInfoText = computed(() => {
  const room = props.appointmentInfo?.roomName || props.appointmentInfo?.room
  const dept = props.appointmentInfo?.deptName || props.appointmentInfo?.department
  if (!room && !dept) return ''
  return [dept, room].filter(Boolean).join(' · ')
})

watch(
  () => props.defaultDestination,
  value => {
    endPoint.value = value || ''
    syncEndSearchLabel()
    clearNav()
  }
)

const openStartDropdown = () => {
  if (startSearch.value === currentStartLabel.value) {
    startSearch.value = ''
  }
  startDropdownOpen.value = true
}

const openEndDropdown = () => {
  if (endSearch.value === currentEndLabel.value) {
    endSearch.value = ''
  }
  endDropdownOpen.value = true
}

const delayedCloseStartDropdown = () => {
  setTimeout(() => {
    startDropdownOpen.value = false
    syncStartSearchLabel()
  }, 120)
}

const delayedCloseEndDropdown = () => {
  setTimeout(() => {
    endDropdownOpen.value = false
    syncEndSearchLabel()
  }, 120)
}

const handleStartSearch = event => {
  startSearch.value = event.target.value
  openStartDropdown()
}

const handleEndSearch = event => {
  endSearch.value = event.target.value
  if (!endSearch.value) {
    endPoint.value = ''
  }
  openEndDropdown()
}

const selectStartOption = option => {
  startPoint.value = option.value
  startSearch.value = option.label
  startDropdownOpen.value = false
  clearNav()
}

const selectEndOption = option => {
  endPoint.value = option.value
  endSearch.value = option.label
  endDropdownOpen.value = false
  clearNav()
}

const selectFirstStartOption = () => {
  const first = filteredStartOptions.value[0]
  if (first) selectStartOption(first)
}

const selectFirstEndOption = () => {
  const first = filteredEndOptions.value[0]
  if (first) selectEndOption(first)
}

const handleClose = () => {
  clearNav()
  startDropdownOpen.value = false
  endDropdownOpen.value = false
  emit('close')
}

const animateNavigation = () => {
  if (!animationState.curve || !avatar.value) return
  if (animationState.progress <= 1) {
    const point = animationState.curve.getPointAt(animationState.progress)
    avatar.value.position.copy(point)
    animationState.progress += animationState.speed
  } else {
    animationState.progress = 0
  }
}

const animate = () => {
  if (sceneState.controls) sceneState.controls.update()
  if (sceneState.renderer && sceneState.scene && sceneState.camera) {
    sceneState.renderer.render(sceneState.scene, sceneState.camera)
    if (sceneState.labelRenderer) {
      sceneState.labelRenderer.render(sceneState.scene, sceneState.camera)
    }
  }
  animateNavigation()
  rafId.value = requestAnimationFrame(animate)
}

const handleResize = () => {
  if (!mountRef.value || !sceneState.camera || !sceneState.renderer || !sceneState.labelRenderer) return
  const { clientWidth, clientHeight } = mountRef.value
  sceneState.camera.aspect = clientWidth / clientHeight
  sceneState.camera.updateProjectionMatrix()
  sceneState.renderer.setSize(clientWidth, clientHeight)
  sceneState.labelRenderer.setSize(clientWidth, clientHeight)
}

const clearNav = () => {
  if (pathMesh.value && sceneState.scene) sceneState.scene.remove(pathMesh.value)
  if (avatar.value && sceneState.scene) sceneState.scene.remove(avatar.value)
  pathMesh.value = null
  avatar.value = null
  animationState.curve = null
  animationState.progress = 0
  isNavigating.value = false
}

const handleNavigate = () => {
  if (!endPoint.value || !sceneState.scene) return

  const startLocation = findOptionByValue(startPoint.value, locationOptions.value) || locationOptions.value[0]
  const targetRoom = findOptionByValue(endPoint.value, destinations.value)

  if (!targetRoom) {
    window.alert('未找到该科室')
    return
  }

  isNavigating.value = true

  const startFloor = startLocation?.floor || 1
  const targetFloor = targetRoom.floor || 1
  const startY = (startFloor - 1) * FLOOR_HEIGHT + 1
  const targetY = (targetFloor - 1) * FLOOR_HEIGHT + 1
  const pathPoints = []

  pathPoints.push(new THREE.Vector3(startLocation.x, startY, startLocation.z))
  pathPoints.push(new THREE.Vector3(startLocation.x, startY, 0))

  if (startFloor !== targetFloor) {
    pathPoints.push(new THREE.Vector3(ELEVATOR_POS.x, startY, 0))
    pathPoints.push(new THREE.Vector3(ELEVATOR_POS.x, startY, ELEVATOR_POS.z + 5))
    pathPoints.push(new THREE.Vector3(ELEVATOR_POS.x, targetY, ELEVATOR_POS.z + 5))
    pathPoints.push(new THREE.Vector3(ELEVATOR_POS.x, targetY, 0))
  } else {
    pathPoints.push(new THREE.Vector3(targetRoom.x, startY, 0))
  }

  const entryZ = targetRoom.z > 0 ? targetRoom.z - 4 : targetRoom.z + 4
  pathPoints.push(new THREE.Vector3(targetRoom.x, targetY, entryZ))
  pathPoints.push(new THREE.Vector3(targetRoom.x, targetY, targetRoom.z))

  if (pathMesh.value) sceneState.scene.remove(pathMesh.value)
  if (avatar.value) sceneState.scene.remove(avatar.value)

  const curve = new THREE.CatmullRomCurve3(pathPoints)
  curve.tension = 0.1
  const geometry = new THREE.TubeGeometry(curve, 64, 0.5, 8, false)
  const material = new THREE.MeshBasicMaterial({ color: 0xff0000, transparent: true, opacity: 0.6 })
  const mesh = new THREE.Mesh(geometry, material)
  sceneState.scene.add(mesh)
  pathMesh.value = mesh

  const avatarGeo = new THREE.SphereGeometry(1.5, 16, 16)
  const avatarMat = new THREE.MeshBasicMaterial({ color: 0x2563eb })
  const avatarMesh = new THREE.Mesh(avatarGeo, avatarMat)
  sceneState.scene.add(avatarMesh)
  avatar.value = avatarMesh

  animationState.curve = curve
  animationState.progress = 0
  animationState.speed = 0.002
}

const buildHospital = scene => {
  const floorGeo = new THREE.BoxGeometry(FLOOR_WIDTH, 1, FLOOR_LENGTH)
  const glassMaterial = new THREE.MeshPhysicalMaterial({
    color: 0xffffff,
    transparent: true,
    opacity: 0.8,
    transmission: 0.2,
    roughness: 0.1
  })

  HOSPITAL_DATA.forEach(floorData => {
    const yPos = (floorData.floor - 1) * FLOOR_HEIGHT
    const group = new THREE.Group()
    group.position.y = yPos

    const floorMesh = new THREE.Mesh(floorGeo, glassMaterial)
    floorMesh.receiveShadow = true
    group.add(floorMesh)

    const floorLabelDiv = document.createElement('div')
    floorLabelDiv.className = 'label floor'
    floorLabelDiv.textContent = `${floorData.floor}F - ${floorData.name}`
    const floorLabel = new CSS2DObject(floorLabelDiv)
    floorLabel.position.set(-FLOOR_WIDTH / 2 - 5, 0, 0)
    group.add(floorLabel)

    const elevatorGeo = new THREE.BoxGeometry(6, 12, 6)
    const elevatorMat = new THREE.MeshLambertMaterial({ color: 0xcccccc })
    const elevator = new THREE.Mesh(elevatorGeo, elevatorMat)
    elevator.position.set(ELEVATOR_POS.x, 6, ELEVATOR_POS.z)
    group.add(elevator)

    const elLabelDiv = document.createElement('div')
    elLabelDiv.className = 'label muted'
    elLabelDiv.textContent = '电梯'
    const elLabel = new CSS2DObject(elLabelDiv)
    elLabel.position.set(0, 13, 0)
    elevator.add(elLabel)

    floorData.rooms.forEach(room => {
      const roomW = room.w || 8
      const roomL = room.l || 8
      const roomH = 5
      let color = 0x4ade80
      const category = room.category || room.dept
      if (category === '外科') color = 0x60a5fa
      if (category === '妇产科') color = 0xf472b6
      if (category === '儿科') color = 0xfacc15
      if (category === '医技') color = 0xa855f7

      const roomMat = new THREE.MeshLambertMaterial({ color, transparent: true, opacity: 0.9 })
      const roomMesh = new THREE.Mesh(new THREE.BoxGeometry(roomW, roomH, roomL), roomMat)
      roomMesh.position.set(room.x, roomH / 2, room.z)
      roomMesh.castShadow = true

      const doorGeo = new THREE.BoxGeometry(2, 4, 0.5)
      const doorMat = new THREE.MeshBasicMaterial({ color: 0x333333 })
      const door = new THREE.Mesh(doorGeo, doorMat)
      if (room.z > 0) door.position.set(0, -0.5, -roomL / 2)
      else door.position.set(0, -0.5, roomL / 2)
      roomMesh.add(door)

      const div = document.createElement('div')
      div.className = 'label room'
      div.textContent = room.id || ''
      const label = new CSS2DObject(div)
      label.position.set(0, roomH / 2 + 2, 0)
      roomMesh.add(label)

      group.add(roomMesh)
    })

    scene.add(group)
  })

  const entranceGeo = new THREE.ConeGeometry(2, 4, 32)
  const entranceMat = new THREE.MeshBasicMaterial({ color: 0xff0000 })
  const entranceMesh = new THREE.Mesh(entranceGeo, entranceMat)
  entranceMesh.rotation.x = Math.PI
  entranceMesh.position.set(ENTRANCE_POS.x, 5, ENTRANCE_POS.z)

  const entLabelDiv = document.createElement('div')
  entLabelDiv.className = 'label start'
  entLabelDiv.textContent = '起点: 医院大门'
  const entLabel = new CSS2DObject(entLabelDiv)
  entLabel.position.set(0, 3, 0)
  entranceMesh.add(entLabel)

  scene.add(entranceMesh)
}

const initScene = () => {
  // Avoid creating duplicate renderers if already initialized
  if (sceneState.scene || !mountRef.value) return
  const width = mountRef.value.clientWidth
  const height = mountRef.value.clientHeight

  // If the popup just appeared, layout may not be ready yet (width/height == 0)
  if ((width < 10 || height < 10) && initTries < 10) {
    initTries += 1
    requestAnimationFrame(initScene)
    return
  }
  initTries = 0

  const scene = new THREE.Scene()
  scene.background = new THREE.Color(0xf0f5f9)
  scene.fog = new THREE.Fog(0xf0f5f9, 50, 500)

  const camera = new THREE.PerspectiveCamera(45, width / height, 0.1, 1000)
  const maxFloor = Math.max(...HOSPITAL_DATA.map(f => f.floor))
  const totalHeight = maxFloor * FLOOR_HEIGHT
  const midHeight = totalHeight / 2
  const cameraHeight = Math.max(120, totalHeight * 0.85)
  camera.position.set(80, cameraHeight, 120)
  camera.lookAt(0, midHeight, 0)

  const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setPixelRatio(window.devicePixelRatio || 1)
  renderer.setSize(width, height)
  renderer.shadowMap.enabled = true
  renderer.domElement.style.position = 'absolute'
  renderer.domElement.style.top = '0'
  renderer.domElement.style.left = '0'
  renderer.domElement.style.width = '100%'
  renderer.domElement.style.height = '100%'
  mountRef.value.appendChild(renderer.domElement)

  const labelRenderer = new CSS2DRenderer()
  labelRenderer.setSize(width, height)
  labelRenderer.domElement.style.position = 'absolute'
  labelRenderer.domElement.style.top = '0'
  labelRenderer.domElement.style.left = '0'
  labelRenderer.domElement.style.width = '100%'
  labelRenderer.domElement.style.height = '100%'
  labelRenderer.domElement.style.pointerEvents = 'none'
  mountRef.value.appendChild(labelRenderer.domElement)

  const ambientLight = new THREE.AmbientLight(0xffffff, 0.6)
  scene.add(ambientLight)
  const dirLight = new THREE.DirectionalLight(0xffffff, 0.8)
  dirLight.position.set(50, 100, 50)
  dirLight.castShadow = true
  dirLight.shadow.mapSize.width = 2048
  dirLight.shadow.mapSize.height = 2048
  scene.add(dirLight)

  const controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.05
  controls.maxPolarAngle = Math.PI / 2 - 0.1
  controls.target.set(0, midHeight, 0)
  controls.update()

  buildHospital(scene)

  sceneState.scene = scene
  sceneState.camera = camera
  sceneState.renderer = renderer
  sceneState.labelRenderer = labelRenderer
  sceneState.controls = controls

  window.addEventListener('resize', handleResize)
  rafId.value = requestAnimationFrame(animate)
}

const disposeScene = () => {
  window.removeEventListener('resize', handleResize)
  if (rafId.value) cancelAnimationFrame(rafId.value)
  if (sceneState.renderer) {
    sceneState.renderer.dispose()
    sceneState.renderer.forceContextLoss?.()
    sceneState.renderer = null
  }
  if (sceneState.labelRenderer) {
    sceneState.labelRenderer.domElement?.remove()
    sceneState.labelRenderer = null
  }
  if (sceneState.scene) {
    sceneState.scene.clear()
    sceneState.scene = null
  }
  sceneState.camera = null
  sceneState.controls = null
  if (mountRef.value) mountRef.value.innerHTML = ''
  initialized.value = false
  initTries = 0
}

const bootSceneIfNeeded = async visible => {
  if (!visible || initialized.value) return
  await nextTick() // Wait for DOM to render (v-if case)
  initScene()
  handleResize()
  initialized.value = true
}

watch(
  () => props.visible,
  value => {
    if (value) {
      bootSceneIfNeeded(true)
    } else {
      disposeScene()
    }
  }
)

onMounted(() => {
  bootSceneIfNeeded(props.visible)
})

onBeforeUnmount(() => {
  disposeScene()
})
</script>

<style scoped>
.nav-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(6px);
  display: grid;
  place-items: center;
  z-index: 2000;
  padding: 1rem;
}

.nav-shell {
  width: min(1200px, 100%);
  height: min(820px, 90vh);
  background: #f8fafc;
  border-radius: 18px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  border: 1px solid #e2e8f0;
}

.nav-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background: linear-gradient(135deg, #2563eb, #60a5fa);
  color: #fff;
}

.nav-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.title-icon {
  width: 36px;
  height: 36px;
}

.eyebrow {
  margin: 0;
  font-size: 0.8rem;
  opacity: 0.9;
}

.headline {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 700;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.35);
  width: 40px;
  height: 40px;
  border-radius: 12px;
  font-size: 1.4rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.35);
}

.nav-body {
  flex: 1;
  position: relative;
  background: radial-gradient(circle at 20% 20%, rgba(96, 165, 250, 0.08), transparent 30%), #e2e8f0;
  overflow: hidden;
}

.nav-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.nav-canvas canvas {
  display: block;
}

.nav-panel {
  position: absolute;
  top: 1.5rem;
  left: 1.5rem;
  width: 340px;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 16px;
  padding: 1.25rem;
  box-shadow: 0 15px 40px rgba(15, 23, 42, 0.25);
  border: 1px solid #e2e8f0;
  backdrop-filter: blur(6px);
}

.panel-section + .panel-section {
  margin-top: 0.75rem;
}

.section-title {
  margin: 0 0 0.35rem 0;
  font-size: 0.85rem;
  font-weight: 700;
  color: #475569;
}

.input-shell {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.65rem 0.8rem;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}

.input-shell.accent {
  border: 1px solid #bfdbfe;
  box-shadow: 0 6px 14px rgba(37, 99, 235, 0.08);
}

.input-shell.muted {
  color: #475569;
}

.input-icon {
  width: 18px;
  height: 18px;
  color: #2563eb;
  flex-shrink: 0;
}

.input-field {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  font-size: 0.95rem;
  color: #0f172a;
}

.search-box {
  position: relative;
}

.dropdown-panel {
  position: absolute;
  left: 0;
  right: 0;
  top: calc(100% + 6px);
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.12);
  z-index: 5;
}

.dropdown-scroll {
  max-height: 240px;
  overflow-y: auto;
}

.dropdown-option {
  width: 100%;
  text-align: left;
  border: none;
  background: transparent;
  padding: 0.6rem 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.dropdown-option:hover {
  background: #eff6ff;
}

.option-label {
  font-weight: 600;
  color: #0f172a;
}

.option-meta {
  font-size: 0.85rem;
  color: #64748b;
}

.hint-line {
  margin: 0.35rem 0 0 0;
  font-size: 0.85rem;
  color: #64748b;
}

.nav-action {
  margin-top: 0.75rem;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  padding: 0.85rem 1rem;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
  border: none;
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 12px 24px rgba(37, 99, 235, 0.22);
  transition: transform 0.15s ease, box-shadow 0.2s ease;
}

.nav-action:disabled {
  background: #cbd5e1;
  cursor: not-allowed;
  box-shadow: none;
}

.nav-action:not(:disabled):hover {
  transform: translateY(-1px);
}

.action-icon {
  width: 18px;
  height: 18px;
}

.legend {
  margin-top: 1rem;
  padding-top: 0.8rem;
  border-top: 1px solid #e2e8f0;
}

.legend-title {
  margin: 0 0 0.5rem 0;
  font-size: 0.8rem;
  color: #94a3b8;
  letter-spacing: 0.4px;
}

.legend-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.35rem 0.75rem;
  font-size: 0.85rem;
  color: #0f172a;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.35rem;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  display: inline-block;
}

.dot.blue {
  background: #60a5fa;
}

.dot.gray {
  background: #cbd5e1;
}

.dot.green {
  background: #4ade80;
}

.dot.orange {
  background: #fb923c;
}

.dot.pink {
  background: #f472b6;
}

.dot.purple {
  background: #a855f7;
}

.nav-hint {
  position: absolute;
  bottom: 1rem;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(15, 23, 42, 0.7);
  color: #fff;
  padding: 0.5rem 1rem;
  border-radius: 999px;
  font-size: 0.9rem;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.15);
}

.label {
  padding: 0.25rem 0.6rem;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.9);
  color: #0f172a;
  border: 1px solid #e2e8f0;
  font-size: 10px;
  white-space: nowrap;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.08);
}

.label.floor {
  background: #2563eb;
  color: #fff;
  border-color: #1d4ed8;
  font-weight: 700;
}

.label.room .room-name {
  font-weight: 700;
  font-size: 11px;
}

.label.room .room-id {
  font-size: 10px;
  color: #475569;
}

.label.muted {
  background: rgba(255, 255, 255, 0.85);
  color: #475569;
}

.label.start {
  background: #ef4444;
  color: #fff;
  border-color: #dc2626;
  font-weight: 700;
}

.nav-fade-enter-active,
.nav-fade-leave-active {
  transition: opacity 0.2s ease;
}

.nav-fade-enter-from,
.nav-fade-leave-to {
  opacity: 0;
}

@media (max-width: 960px) {
  .nav-panel {
    position: static;
    width: auto;
    margin: 1rem;
  }

  .nav-shell {
    height: 95vh;
  }
}
</style>
