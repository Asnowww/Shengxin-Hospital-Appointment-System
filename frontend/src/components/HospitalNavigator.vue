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
              <div class="input-shell muted">
                <svg xmlns="http://www.w3.org/2000/svg" class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 10c0 7-9 12-9 12S3 17 3 10a9 9 0 0 1 18 0" />
                  <circle cx="12" cy="10" r="3" />
                </svg>
                <select v-model="startPoint" class="input-field" aria-label="起点">
                  <option value="entrance">医院大门（默认）</option>
                </select>
              </div>
            </div>

            <div class="panel-section">
              <p class="section-title">前往科室</p>
              <div class="input-shell accent">
                <svg xmlns="http://www.w3.org/2000/svg" class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="11" cy="11" r="8" />
                  <path d="m21 21-4.35-4.35" />
                </svg>
                <select
                  v-model="endPoint"
                  @change="clearNav"
                  class="input-field"
                  aria-label="选择目的地"
                >
                  <option value="">请选择目的地...</option>
                  <option
                    v-for="dest in destinations"
                    :key="dest.value"
                    :value="dest.value"
                  >
                    {{ dest.label }}
                  </option>
                </select>
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
                <div class="legend-item"><span class="dot blue"></span>5F 外科/妇儿</div>
                <div class="legend-item"><span class="dot gray"></span>4F 检查/行政</div>
                <div class="legend-item"><span class="dot gray"></span>3F 手术/疼痛</div>
                <div class="legend-item"><span class="dot gray"></span>2F 专科门诊</div>
                <div class="legend-item"><span class="dot green"></span>1F 内科/大厅</div>
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

const HOSPITAL_DATA = [
  {
    floor: 1,
    name: '门诊一楼',
    description: '内科综合诊区',
    rooms: [
      { id: '101', name: '老年病科', dept: '内科', x: -15, z: -10, desc: '101诊室' },
      { id: '102', name: '神经内科', dept: '内科', x: -15, z: 0, desc: '102诊室' },
      { id: '103', name: '内分泌科', dept: '内科', x: -15, z: 10, desc: '103诊室' },
      { id: '104', name: '呼吸内科', dept: '内科', x: 15, z: -10, desc: '104诊室' },
      { id: '105', name: '高血压科', dept: '内科', x: 15, z: 0, desc: '105诊室' },
      { id: '112', name: '风湿免疫科', dept: '内科', x: 15, z: 10, desc: '112诊室' },
      { id: 'lobby', name: '服务台', dept: '公共', x: 0, z: 15, desc: '大厅服务台' }
    ]
  },
  {
    floor: 2,
    name: '门诊二楼',
    description: '专科门诊',
    rooms: [
      { id: '201', name: '口腔科', dept: '五官科', x: -15, z: -10, desc: '201诊室' },
      { id: '202', name: '眼科', dept: '五官科', x: -15, z: 0, desc: '202诊室' },
      { id: '203', name: '耳鼻喉科', dept: '五官科', x: -15, z: 10, desc: '203诊室' },
      { id: '204', name: '皮肤科', dept: '皮肤科', x: 15, z: -10, desc: '204诊室' },
      { id: '205', name: '中医科', dept: '中医', x: 15, z: 0, desc: '205诊室' },
      { id: '206', name: '康复医学科', dept: '康复', x: 15, z: 10, desc: '206诊室' }
    ]
  },
  {
    floor: 3,
    name: '门诊三楼',
    description: '日间手术与疼痛',
    rooms: [
      { id: '301', name: '疼痛科', dept: '麻醉科', x: -15, z: -10, desc: '301诊室' },
      { id: '302', name: '麻醉门诊', dept: '麻醉科', x: -15, z: 0, desc: '302诊室' },
      { id: '303', name: '日间手术中心', dept: '手术', x: 15, z: 0, w: 10, l: 25, desc: '日间手术区域' }
    ]
  },
  {
    floor: 4,
    name: '门诊四楼',
    description: '功能检查区',
    rooms: [
      { id: '401', name: '超声科', dept: '检查', x: -15, z: -5, w: 8, l: 15, desc: 'B超/彩超' },
      { id: '402', name: '心电图室', dept: '检查', x: -15, z: 10, desc: 'ECG' },
      { id: '403', name: '内镜中心', dept: '检查', x: 15, z: -5, w: 8, l: 15, desc: '胃肠镜' },
      { id: '404', name: '行政办公室', dept: '行政', x: 15, z: 12, desc: '院办' }
    ]
  },
  {
    floor: 5,
    name: '门诊五楼',
    description: '外科与妇儿中心',
    rooms: [
      { id: '501', name: '神经外科', dept: '外科', x: -15, z: -12, desc: '501诊室' },
      { id: '502', name: '骨科', dept: '外科', x: -15, z: -4, desc: '502诊室' },
      { id: '504', name: '烧伤整形科', dept: '外科', x: -15, z: 4, desc: '504诊室' },
      { id: '507', name: '心脏外科', dept: '外科', x: -15, z: 12, desc: '507诊室' },
      { id: '508', name: '妇科', dept: '妇产科', x: 15, z: -12, desc: '508诊室' },
      { id: '509', name: '普胸外科', dept: '外科', x: 15, z: -4, desc: '509诊室' },
      { id: '511', name: '小儿外科', dept: '儿科', x: 15, z: 4, desc: '511诊室' },
      { id: '512', name: '小儿内科', dept: '儿科', x: 15, z: 12, desc: '512诊室' }
    ]
  }
]

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
    clearNav()
  }
)

const handleClose = () => {
  clearNav()
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
  isNavigating.value = true

  let targetRoom = null
  let targetFloor = 1
  HOSPITAL_DATA.forEach(f => {
    const room = f.rooms.find(r => r.id === endPoint.value)
    if (room) {
      targetRoom = room
      targetFloor = f.floor
    }
  })

  if (!targetRoom) {
    window.alert('未找到该科室')
    return
  }

  const pathPoints = []
  const startY = 1
  pathPoints.push(new THREE.Vector3(ENTRANCE_POS.x, startY, ENTRANCE_POS.z))
  pathPoints.push(new THREE.Vector3(ENTRANCE_POS.x, startY, 0))

  if (targetFloor > 1) {
    pathPoints.push(new THREE.Vector3(ELEVATOR_POS.x, startY, 0))
    pathPoints.push(new THREE.Vector3(ELEVATOR_POS.x, startY, ELEVATOR_POS.z + 5))
    const targetY = (targetFloor - 1) * FLOOR_HEIGHT + 1
    pathPoints.push(new THREE.Vector3(ELEVATOR_POS.x, targetY, ELEVATOR_POS.z + 5))
    pathPoints.push(new THREE.Vector3(ELEVATOR_POS.x, targetY, 0))
  }

  const targetBaseY = (targetFloor - 1) * FLOOR_HEIGHT + 1
  pathPoints.push(new THREE.Vector3(targetRoom.x, targetBaseY, 0))
  const entryZ = targetRoom.z > 0 ? targetRoom.z - 4 : targetRoom.z + 4
  pathPoints.push(new THREE.Vector3(targetRoom.x, targetBaseY, entryZ))
  pathPoints.push(new THREE.Vector3(targetRoom.x, targetBaseY, targetRoom.z))

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

  HOSPITAL_DATA.forEach((floorData, index) => {
    const yPos = index * FLOOR_HEIGHT
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
      if (room.dept === '外科') color = 0x60a5fa
      if (room.dept === '妇产科') color = 0xf472b6
      if (room.dept === '儿科') color = 0xfacc15

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
      div.innerHTML = `
        <div class="room-name">${room.name}</div>
        <div class="room-id">${room.id || ''}</div>
      `
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
  camera.position.set(80, 120, 120)
  camera.lookAt(0, 40, 0)

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
  controls.target.set(0, FLOOR_HEIGHT * 1.5, 0)
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
