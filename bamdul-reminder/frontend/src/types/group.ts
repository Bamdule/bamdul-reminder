export type GroupPermission = "READ" | "READ_WRITE";

export interface Group {
  id: number;
  name: string;
  ownerId: number;
  ownerName: string;
  createdAt: string;
  updatedAt: string;
}

export interface GroupMember {
  id: number;
  memberId: number;
  memberName: string;
  memberEmail: string;
  permission: GroupPermission;
  joinedAt: string;
}
