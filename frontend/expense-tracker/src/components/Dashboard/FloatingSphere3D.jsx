import { Canvas, useFrame } from "@react-three/fiber";
import { Float, MeshDistortMaterial, OrbitControls } from "@react-three/drei";
import { useMemo, useRef } from "react";

const Particles = () => {
  const groupRef = useRef();
  const particles = useMemo(
    () =>
      Array.from({ length: 26 }, (_, i) => ({
        id: i,
        position: [
          (Math.random() - 0.5) * 4.2,
          (Math.random() - 0.5) * 3.2,
          (Math.random() - 0.5) * 3.6,
        ],
        scale: 0.04 + Math.random() * 0.07,
      })),
    []
  );

  useFrame((state) => {
    if (groupRef.current) {
      groupRef.current.rotation.y = state.clock.elapsedTime * 0.16;
    }
  });

  return (
    <group ref={groupRef}>
      {particles.map((p) => (
        <mesh key={p.id} position={p.position} scale={p.scale}>
          <sphereGeometry args={[1, 14, 14]} />
          <meshStandardMaterial
            color="#ffc6ff"
            emissive="#ff8fab"
            emissiveIntensity={0.5}
            transparent
            opacity={0.85}
          />
        </mesh>
      ))}
    </group>
  );
};

const Orb = () => {
  const orbRef = useRef();
  useFrame((state) => {
    if (!orbRef.current) return;
    const t = state.clock.elapsedTime;
    orbRef.current.rotation.y = t * 0.24;
    orbRef.current.rotation.x = Math.sin(t * 0.7) * 0.14;
  });

  return (
    <Float speed={1.5} rotationIntensity={0.85} floatIntensity={1.05}>
      <mesh ref={orbRef} scale={1.05}>
        <sphereGeometry args={[1, 64, 64]} />
        <MeshDistortMaterial
          color="#ff8fab"
          emissive="#ff8fab"
          emissiveIntensity={0.34}
          transparent
          opacity={0.56}
          distort={0.24}
          speed={2.1}
          roughness={0.16}
          metalness={0.18}
        />
      </mesh>
    </Float>
  );
};

const Scene = () => {
  const parallaxRef = useRef();
  useFrame((state) => {
    if (!parallaxRef.current) return;
    const tx = state.pointer.x * 0.34;
    const ty = state.pointer.y * 0.24;
    parallaxRef.current.rotation.y += (tx - parallaxRef.current.rotation.y) * 0.06;
    parallaxRef.current.rotation.x += (-ty - parallaxRef.current.rotation.x) * 0.06;
  });

  return (
    <group ref={parallaxRef}>
      <ambientLight intensity={1.1} />
      <pointLight position={[2, 2, 3]} color="#ff8fab" intensity={18} />
      <pointLight position={[-2, -1, 2]} color="#ffc6ff" intensity={8} />
      <Particles />
      <Orb />
      <OrbitControls enablePan={false} enableZoom={false} autoRotate autoRotateSpeed={0.55} />
    </group>
  );
};

const FloatingSphere3D = () => {
  return (
    <div className="card h-[260px] md:h-[320px] overflow-hidden">
      <Canvas camera={{ position: [0, 0, 3.4], fov: 52 }}>
        <Scene />
      </Canvas>
    </div>
  );
};

export default FloatingSphere3D;
