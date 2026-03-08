export interface Member {
  id: number;
  email: string;
  name: string;
  createdAt: string;
}

export interface AuthResult {
  token: string;
  member: Member;
}
