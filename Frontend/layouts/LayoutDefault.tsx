import "./tailwind.css";
import React, {ReactNode} from "react";

export default function LayoutDefault({children,}: Readonly<{ children: ReactNode; }>) {
    return (
        <div className="flex max-w-5xl m-auto">
            <Content>{children}</Content>
        </div>
    );
}


function Content({children}: Readonly<{ children: React.ReactNode }>) {
    return (
        <div className="p-5 pb-12 min-h-screen container mx-auto">
            <h1 className="font-bold text-3xl pb-4">Оцифровка трудовых книжек</h1>
            {children}
        </div>
    );
}

