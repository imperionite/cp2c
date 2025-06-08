import { atomWithStorage } from "jotai/utils";
import { atom } from "jotai";

export const authAtom = atomWithStorage("authAtom", {
  id: "",
  username: "",
  token: ""
});

export const employeesAtom = atom([]);