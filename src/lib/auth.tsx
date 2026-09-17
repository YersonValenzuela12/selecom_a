import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import type { Session, User } from '@supabase/supabase-js';
import { supabase } from '@/lib/supabase';
import type { Role } from '@/data/mockData';

export interface Profile {
  id: string;
  email: string;
  full_name: string;
  role: Role;
  title: string;
  phone: string;
  status: 'active' | 'suspended';
  region: string;
  avatar_color: string;
  initials: string;
  last_login: string | null;
}

interface AuthContextValue {
  session: Session | null;
  user: User | null;
  profile: Profile | null;
  loading: boolean;
  signIn: (email: string, password: string) => Promise<{ error: string | null }>;
  signInDemo: (role: Role) => void;
  signOut: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(null);
  const [profile, setProfile] = useState<Profile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    supabase.auth.getSession().then(({ data }) => {
      setSession(data.session);
      if (data.session) {
        loadProfile(data.session.user.id);
      } else {
        setLoading(false);
      }
    });

    const { data: listener } = supabase.auth.onAuthStateChange((_event, sess) => {
      setSession(sess);
      if (sess) {
        (async () => { await loadProfile(sess.user.id); })();
      } else {
        setProfile(null);
        setLoading(false);
      }
    });

    return () => { listener.subscription.unsubscribe(); };
  }, []);

  async function loadProfile(userId: string) {
    const { data, error } = await supabase
      .from('profiles')
      .select('*')
      .eq('id', userId)
      .maybeSingle();

    if (error) {
      console.error('Profile load error:', error.message);
    }
    setProfile(data as Profile | null);
    setLoading(false);
  }

  async function signIn(email: string, password: string) {
    const { error } = await supabase.auth.signInWithPassword({ email, password });
    return { error: error?.message ?? null };
  }

  function signInDemo(demoRole: Role) {
    const demoProfiles: Record<Role, Profile> = {
      admin: {
        id: 'u-001',
        email: 'rachel.whitman@selecom.com',
        full_name: 'Rachel Whitman',
        role: 'admin',
        title: 'System Administrator',
        phone: '+51 955 220 182',
        status: 'active',
        region: 'HQ — San Francisco',
        avatar_color: 'bg-primary-600',
        initials: 'RW',
        last_login: 'Hoy 08:14'
      },
      supervisor: {
        id: 'u-014',
        email: 'marcus.delgado@selecom.com',
        full_name: 'Marcus Delgado',
        role: 'supervisor',
        title: 'Supervisor de Operaciones',
        phone: '+51 915 550 144',
        status: 'active',
        region: 'Surco',
        avatar_color: 'bg-emerald-600',
        initials: 'MD',
        last_login: 'Hoy 07:42'
      },
      coordinador: {
        id: 'u-038',
        email: 'maria.gonzalez@selecom.com',
        full_name: 'Maria Gonzalez',
        role: 'coordinador',
        title: 'Coordinador de Operaciones',
        phone: '+51 915 555 144',
        status: 'active',
        region: 'Lima Metropolitana',
        avatar_color: 'bg-teal-600',
        initials: 'MG',
        last_login: 'Hoy 07:42'
      },
      technician: {
        id: 'u-031',
        email: 'daniel.okafor@selecom.com',
        full_name: 'Daniel Okafor',
        role: 'technician',
        title: 'Senior Field Technician',
        phone: '+51 914 155 173',
        status: 'active',
        region: 'Surquillo',
        avatar_color: 'bg-amber-600',
        initials: 'DO',
        last_login: 'Hoy 06:55'
      }
    };
    const prof = demoProfiles[demoRole];
    setProfile(prof);
    setSession({ user: { id: prof.id, email: prof.email } } as any);
    setLoading(false);
  }

  async function signOut() {
    try { await supabase.auth.signOut(); } catch (_) {}
    setProfile(null);
    setSession(null);
  }

  return (
    <AuthContext.Provider value={{
      session, user: session?.user ?? null, profile, loading, signIn, signInDemo, signOut,
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
