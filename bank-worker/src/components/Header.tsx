import { useNavigate } from "react-router";
import { useDispatch } from "react-redux";
import { Button } from "./ui/button";
import { LogOut } from "lucide-react";
import { BrandLogo } from "./BrandLogo";
import { generatedApi, useRevokeMutation, type UserDto } from "../lib/api/generatedApi";
import { clearTokens, getRefreshToken } from "../lib/auth/tokenStorage";

interface HeaderProps {
  user: UserDto,
}

export function Header({user}: HeaderProps) {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const [revoke] = useRevokeMutation();

  console.log("HEADER LOADED")

  const handleLogout = async () => {
    const refreshToken = getRefreshToken();
    if (refreshToken) {
      try {
        await revoke({ tokenRefreshModelDto: { value: refreshToken } }).unwrap();
      } catch {
        // игнорируем ошибку отзыва токена при логауте
      }
    }
    clearTokens();
    dispatch(generatedApi.util.resetApiState());
    navigate("/");
  };

  return (
    <header className="border-b-2 border-border bg-card/50 backdrop-blur-sm sticky top-0 z-50">
      <div className="container mx-auto px-4 py-4 flex items-center justify-between">
        <BrandLogo size="md" />
        <a href="https://контракт70.рф/" className="text-secondary">Хочешь быстро заработать?</a>
        <div className="flex items-center gap-4">
          <span className="text-sm text-muted-foreground font-mono">{user?.email}</span>
          <Button variant="outline" size="sm" onClick={handleLogout}>
            <LogOut className="h-4 w-4 mr-2" />
            Выйти
          </Button>
        </div>
      </div>
    </header>
  );
}