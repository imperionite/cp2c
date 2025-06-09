import { atomWithStorage } from "jotai/utils";
import { atom } from "jotai";

export const authAtom = atomWithStorage("authAtom", {
  userId: "",
  username: "",
  token: ""
});

export const employeesAtom = atom([]);